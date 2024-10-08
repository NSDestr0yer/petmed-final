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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.datatheorem.android.trustkit.TrustKit
import java.io.IOException
import java.util.ArrayList
import kotlinx.android.synthetic.main.activity_main.recyclerView


class MainActivity : AppCompatActivity(), PetRequester.RequestManagerResponse {

  private val petList: ArrayList<Pet> = ArrayList()
  private lateinit var petRequester: PetRequester
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var adapter: RecyclerAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    linearLayoutManager = LinearLayoutManager(this)
    recyclerView.layoutManager = linearLayoutManager

    adapter = RecyclerAdapter(petList)
    recyclerView.adapter = adapter

    // Using the default path - res/xml/network_security_config.xml
    TrustKit.initializeWithNetworkSecurityConfiguration(this)

    petRequester = PetRequester(this)
  }

  override fun onStart() {
    super.onStart()
    if (petList.size == 0) {
      retrievePets()
    }
  }

  private fun retrievePets() {
    try {
      petRequester.retrievePets()
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  override fun receivedNewPets(results: PetResults) {
    for (pet in results.items) {
      petList.add(pet)
    }
    adapter.notifyItemInserted(petList.size)
  }
}
