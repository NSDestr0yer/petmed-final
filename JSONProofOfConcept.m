#import "JSONProofOfConcept.h"

@implementation JSONProofOfConcept

/*
NSInteger DateSort(NSString *pathString1, NSString *pathString2, void *context)
{
    NSFileManager *fileManager = (NSFileManager *)context;
    NSError *error = nil;
    NSDictionary *attributeDictionary1 = [fileManager attributesOfItemAtPath:pathString1 error:&error];
    NSDictionary *attributeDictionary2 = [fileManager attributesOfItemAtPath:pathString2 error:&error];

    NSDate *createdDate1 = [attributeDictionary1 fileCreationDate];
    NSDate *createdDate2 = [attributeDictionary2 fileCreationDate];

    return [createdDate1 compare:createdDate2];
}
*/

- (NSString *)_stringByConvertingCommas:(id)object
{
    if (object) {
        return [[object description] stringByReplacingOccurrencesOfString:@"," withString:@"-"]; 
    }
    else {
        return @"";
    }
}

//https://anonymousoverflow.privacyredirect.com/questions/29615196/is-csv-with-multi-tabs-sheet-possible
- (NSString *)_convertToCSV:(NSDictionary *)dictionary
{
    NSString *uuidString = [[NSProcessInfo processInfo] globallyUniqueString];
    if ([dictionary isKindOfClass:[NSDictionary class]]) {
        
        NSMutableString *csvString = [[NSMutableString alloc] init];
        NSMutableString *definitionString = [[NSMutableString alloc] init];
        NSMutableString *valueString = [[NSMutableString alloc] init];
        
        for (NSString *keyString in dictionary) {
            id value = [dictionary objectForKey:keyString];
            
            if ([_exclusionSet containsObject:keyString]) {
                [definitionString appendString:keyString];
                [definitionString appendString:@","];
            
                [valueString appendString:[self _stringByConvertingCommas:value]];
                [valueString appendString:@","];
            }
            else if ([value isKindOfClass:[NSDictionary class]]) {
            
                [definitionString appendString:keyString];
                [definitionString appendString:@","];
                
                NSString *returnedUUIDString = [self _convertToCSV:value];
                NSString *referenceString = [[NSString alloc] initWithFormat:@"REF#:%@", returnedUUIDString];
                [valueString appendString:referenceString];
                [referenceString release];
                [valueString appendString:@","];
            }
            else if ([value isKindOfClass:[NSArray class]]) {
                [definitionString appendString:keyString];
                [definitionString appendString:@","];
                
                NSMutableString *arrayString = [[NSMutableString alloc] initWithString:@"["];
                for (id arrayValue in value) {
                    NSString *returnedUUIDString = [self _convertToCSV:arrayValue];
                    [arrayString appendString:@"REF#:"];
                    [arrayString appendString:returnedUUIDString];
                    [arrayString appendString:@" | "];
                }
                [arrayString appendString:@"]"];
                [valueString appendString:arrayString];
                [valueString appendString:@","];
                [arrayString release];
            }
            else {
                [definitionString appendString:keyString];
                [definitionString appendString:@","];
            
                [valueString appendString:[self _stringByConvertingCommas:value]];
                [valueString appendString:@","];
            }
        }
            
        [csvString appendString:definitionString];
        [csvString appendString:@"\n"];
        [csvString appendString:valueString];
        [definitionString release];
        [valueString release];
        
        NSString *filenameString = [[NSString alloc] initWithFormat:@"/out/REF-%@.csv", uuidString];
        [csvString writeToFile:filenameString atomically:YES];
        [filenameString release];
        [csvString release];
    }
    
    return uuidString;
}

- (void)exportJSONTest
{
    _exclusionSet = [[NSSet alloc] initWithObjects:@"internalInfo", nil];
    
    NSDictionary *dictionary = [[NSMutableDictionary alloc] init];
    
    NSDictionary *reportDetails = [[NSMutableDictionary alloc] init];
    [reportDetails setValue:@"OSPCA-Toronto" forKey:@"orgID"];
    [reportDetails setValue:@"AnimalWelfare-PAWS" forKey:@"reportType"];
    
    NSDictionary *internal = [[NSMutableDictionary alloc] init];
    [internal setValue:@"AAAB" forKey:@"internalID"];
    [internal setValue:@"Started" forKey:@"internalStatus"];
    
    NSMutableArray *transactionsArray = [[NSMutableArray alloc] init];
    NSDictionary *transaction1 = [[NSMutableDictionary alloc] init];
    [transaction1 setValue:@"1" forKey:@"evidenceID"];
    [transaction1 setValue:@"Call to PAWS, lots of cars on Saturday bringing down into the suspect's house" forKey:@"evidenceDetails"];
    NSDictionary *transaction2 = [[NSMutableDictionary alloc] init];
    [transaction2 setValue:@"2" forKey:@"evidenceID"];
    [transaction2 setValue:@"Ontario Wildlife: Solve a natural resource case called in a tip - illegal hunting person which matches the address of suspect's house" forKey:@"evidenceDetails"];
    [transactionsArray addObject:transaction1];
    [transactionsArray addObject:transaction2];
    [dictionary setValue:transactionsArray forKey:@"evidenceList"];
    [transactionsArray release];
    [transaction1 release];
    [transaction2 release];
    
    [dictionary setValue:internal forKey:@"internalInfo"];
    [dictionary setValue:reportDetails forKey:@"reportInfo"];
    [dictionary setValue:@"123" forKey:@"reportID"];
    [dictionary setValue:@"Ontario PAWS hotline was called on person x for underground dog fighting ring" forKey:@"reportDescription"];

    [self _convertToCSV:dictionary];
    
    [internal release];
    [reportDetails release];
    [dictionary release];
    [_exclusionSet release];
    _exclusionSet = nil;
    NSLog(@"Done");
}

- (NSSet *)_newExclusionSetFromFilename:(NSString *)filenameString
{
    NSMutableSet *set = [[NSMutableSet alloc] init];
    if (filenameString) {
        NSString *excludeString = [[NSString alloc] initWithContentsOfFile:filenameString];
        if (excludeString) {
            NSArray *excludeArray = [[excludeString stringByTrimmingCharactersInSet:[NSCharacterSet newlineCharacterSet]] componentsSeparatedByString:@","];
            if (excludeArray) {
                for (NSString *nodeToExcludeString in excludeArray) {
                    if (nodeToExcludeString) {
                        [set addObject:nodeToExcludeString];
                    }
                }
            }
            [excludeString release];
        }
    }
    return set;
}

- (void)exportJSONFile:(NSString *)filenameString withExclusionFile:(NSString *)exclusionString;
{
    if (filenameString) {
        NSData *data = [[NSData alloc] initWithContentsOfFile:filenameString];
        if (data) {
            NSError *error = nil;
            NSDictionary *jsonDictionary = [NSJSONSerialization JSONObjectWithData:data options:0 error:&error];
            if (jsonDictionary) {
                _exclusionSet = [self _newExclusionSetFromFilename:exclusionString];
                [self _convertToCSV:jsonDictionary];
                [_exclusionSet release];
                _exclusionSet = nil;
            }
            else {
                NSLog(@"Could not parse JSON.");
            }
            
            if (error) {
                NSLog(@"Error: %@", [error description]);
            }
            
            [data release];
        }
        else {
            NSLog(@"Could not open file.");
        }
    }
    else {
        NSLog(@"No filename(s) passed.");
    }
}

@end
