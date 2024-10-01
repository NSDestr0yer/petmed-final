#import <AppKit/AppKit.h>

@interface JSONProofOfConcept : NSObject
{
    NSSet *_exclusionSet;
}
-(void)exportJSONTest;
-(void)exportJSONFile:(NSString *)filenameString withExclusionFile:(NSString *)exclusionString;
@end
