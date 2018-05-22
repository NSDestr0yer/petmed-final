/*
 * Copyright (c) 2018 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.petmed

import android.app.Activity
import android.content.Context
import android.util.Base64
import android.util.Log
import com.datatheorem.android.trustkit.TrustKit
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

private const val SERVER_PUBLIC_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEP9M/My4tmNiaZRcQtYj58EjGN8N3uSnW/s7FpTh4Q+T3tNVkwVCjmDN+a2qIRTcedQyde0d8CoG3Lp2ZlnPhcw=="

class PetRequester(listeningActivity: Activity) {

  interface RequestManagerResponse {
    fun receivedNewPets(results: PetResults)
  }

  private val responseListener: RequestManagerResponse
  private val context: Context

  init {
    responseListener = listeningActivity as RequestManagerResponse
    context = listeningActivity.applicationContext
  }

  fun retrievePets() {

    val urlString = "https://collinstuart.github.io/posts.json" //1
    val url = URL(urlString)

    // Sign request
    val authenticator = Authenticator() //2
    val bytesToSign = urlString.toByteArray(Charsets.UTF_8)
    val signedData = authenticator.sign(bytesToSign)
    val requestSignature = Base64.encodeToString(signedData, Base64.DEFAULT)
    Log.d("PetRequester", "signature for request : $requestSignature")

    // Check signing worked
    val signingSuccess = authenticator.verify(signedData, bytesToSign) //3
    Log.d("PetRequester", "success : $signingSuccess")

    // Start connection
    val connection = url.openConnection() as HttpsURLConnection //4
    connection.sslSocketFactory = TrustKit.getInstance().getSSLSocketFactory(url.host)

    doAsync {
      val json = connection.inputStream.bufferedReader().readText() //5
      connection.disconnect()

      uiThread {

        // Verify received signature
        val jsonElement = JsonParser().parse(json) //6
        val jsonObject = jsonElement.asJsonObject
        val result = jsonObject.get("items").toString()
        val resultBytes = result.toByteArray(Charsets.UTF_8)

        val signature = jsonObject.get("signature").toString() //7
        val signatureBytes = android.util.Base64.decode(signature, android.util.Base64.DEFAULT)
        val success = authenticator.verify(signatureBytes, resultBytes, SERVER_PUBLIC_KEY)

        if (success) {
          // Process data
          val receivedPets = Gson().fromJson(json, PetResults::class.java) //8
          responseListener.receivedNewPets(receivedPets)
        }

      }
    }
  }
}