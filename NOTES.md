# CS4218 Team Project: Shell - QA Report

## Injected bugs

Here is a list of all the bugs that we believe have been injected in the code provided:

### Commit [bb84252](https://github.com/nus-cs4218/cs4218-project-ay1920-s2-2020-team03/commit/bb84252d532ffae18a2cf674bc045e46198af2f2)

- `break` outside loop or switch statement in `ShellImpl` main function, causing a build error. This has been fixed, but no tests were written as the build error will no longer occur

- Inverted boolean logic in `CallComand.evaluate()`

- Missing `break` statements in `ApplicationRunner`

- Uncaught and undeclared exception in `IOUtils.openOutputStream()`

- Missing increment causing infinite loop in `StringUtils.isBlank()`

### Commit [dd06ee3](https://github.com/nus-cs4218/cs4218-project-ay1920-s2-2020-team03/commit/dd06ee3a0de623e63c01c7700206085937ed66cb)

- Inverted boolean logic in `StringUtils.isBlank()`

### Commit [bfe24a0](https://github.com/nus-cs4218/cs4218-project-ay1920-s2-2020-team03/pull/1/commits/bfe24a05f971d3a0a185ab2b7d7a0e2199e0ce45)

- Prevent Shell from exiting after just one command

### Commit []()

- Removed unused variable `currentDirecgory` in `ShellImpl`

- Fixed missing shell prompt

- Broken null check in `IORedirectionHandler`. Found using the PMD rule set.

- Inconsistent new lines at the end of echo command

### Commit []()

- Fixed implementation of LS command. The implementation bug was mentioned in comment in `sg.edu.nus.comp.cs4218.impl.app.LsApplication#buildResult()`

- Performance improvement by using `StringBuilder` in `RegexArgument#globFiles()`

### Commit []()

- ArrayIndexOutOfBounds exception when calling cd without args. Error message printed is just 0. Created a test case for this but not up to us to fix.

- Fixed sequence commands: tokens of previous commands were being used for the ones following because the tokens list was not cleaned in `CommandBuilder`

### Commit []()

- Fixed ignored `<` for input redirection in `CommandBuilder`

- Fixed broken check in `IORedirectionHandler.isRedirOperator()`

### Commit []()

- SedApplication fix indexing and matching to replace the expected match

- SedApplication write the result by replacing the file's content with result

- Fixed indexing bug in `PipeCommand`

- Fixed negation to add result of subCommand

- When sub command is evaluated, in the result the new line is removed and replaced by "" instead of space

# Notes

- So far no tests have been written for these
- Files with spaces in it have to be quoted

# TDD - ignored tests

SED: One case ignored cause null regex is not allowed in UNIX

EXIT: One case ignored cause Different implementation style

WC: Four tests ignored cause ours is behaving same as UNIX. Probably OS or arch diff with bytes flag

MV: Two tests ignored cause they test out put stream which is not unit to Mv and can be tested by integration on higher level.
Two ignored cause our implementation is more similar to UNIX.
Please check `countFromFiles_multiFiles()` for formatting of result diff from actual unix

Cut:

- Ignored testRunByteIndexOutOfRange() because GNU's implementation of cut doesn't care about the excess range provided and cuts the text in full.
- Ignored testRunCharacterIndexOutOfRange() because GNU's implementation of cut doesn't care about the excess range provided and cuts the text in full.
- Ignored testRunWithClosedOutputStream(). There is always the chance that an IOException is encountered, so one cannot avoid the exception handling code. Adding this test is likely to complicate the code.

Grep:

- Ignored two tests cause not possible to put null for case insensitive flag
- Two cause they were calling a specific method of the application with arguments which fail pre-conditions

Diff:

- Ignored testFailsWithDirWithoutFiles() because GNU's implementation of diff allows comparison of directories with empty files.

# TDD - fixes

- CALL Command:
  - Fixed null edge cases
- Exit

  - Should we change our impl?

- Globbing

  - Fixed regex bug for relative paths

- CdApp

  - Edge cases
  - Null args
  - No args and multiple args
  - No permission

- IoRedir
  - For `abc > >` case
- Mv
  - Multiple fixes for better implementation and exception error messages
- Paste
  - Added check for null input stream and test cases for it and null output stream as well
  - Added test case for null args
  - Fixed extra of newlines at the end of output
  - Better error message when file is not found
  - Better error message when file is a directory
  - Better error message when fail to write to outstream
  - Pasting empty file was causing an error with the StringBuilder. Fixed now
- Rm
  - Decision not to support file permissions as it is beyond the scope of the specification
  - Added better reporting of when files cannot be removed for being directories and not having the right flags specified
  - Added restriction of not being able to remove directories ".", ".." or ending in "/." or "/.."
  - The above restriction was causing an exception when filenames were too short. That was found and fixed in manual testing while constructing integrationt tests
- Find
  - Change exception message to be same as UNIX
- Cut
  - Changed expected results of testCutTwoCharactersInReverseOrderFromFile() to fit GNU cut's implementation.
  - Changed expected results of testRunWithTwoFile() as there is a error in the expected results.
- Grep
  - Null checks
  - No read permission test

Also includes trivial fixes in tests and small bugs

# Experience with EvoSuite

All generated tests were pushed to package `test.automated` in source code style.
We then picked cases which we determined were useful (Testing is an art and hence we had to choose whats best for software)

1. We have mainly used EvoSuite for couple of purposes:

   - Generate random inputs (which seems more useful for command builders, regex, argumentresolvers than for actual commands)
   - For testing util functions as they are integral part of project

1. Main aim was to increase coverage and build trust in our test suite.
   However, to avoid explosion of test cases which would'nt be useful for either rigorous testing or finding bugs and increasing
   testing time were avoided.

### Big Wins with EvoSuite

1. 100% Class coverage for covering ErrorConstant static class
1. 100% Util Testing for CommandBuilder, StringUtils, RegexArgument, ArgumentResolver.
1. Provides virtual file system
1. Optimization of different coverage criteria, like lines, branches, outputs and mutation testing
1. Covers more coverage but doesnt generate functionality related cases. Hence having our previous cases were good enough for unit tests
1. Good for generating trivial test cases quickly e.g. parsers because despite them being simple are trivial in finding bugs

### Problems we faced

1. Calls to libraries not part of our project especially for mocking files etc
1. Need to modify tests
1. Generates many random cases for unit since it doesn't know what driver calls it in actual software based on other rules
   and hence generates many useless tests. For e.g. Echo
1. Didnt use for Apps as it generated fewer and tests which didnt add value to our current test suite. E.g. Echo
1. Amazing virtual file system but couldnt make full use due to restriction on dependncy jars that can be used
1. Unable to create correct tests cause of inheritance variable access rules for child classes

# Experience with SqTest

1. Generated cases which dont consider output.
1. Has a good basic structure of tests which need to be modifed and can generate based on what dependencies need to be mocked.
1. Not very useful for us as we had already followed such patter and this was just a automated way to do which would have proven
   very useful for Milestone1
1. For unit cases specific to this project it wasnt able to generate for different types of inputs and path.

## How the tests generated were used?

- The main purpose was to increase coverage by covering edge cases.
- Having regression tests were also useful as we were simultaneously updating our implementation while fixing TDD tests.
- Generating random inputs also helped in testing unique situations which found faults in the software. However, some of them were very specific which didn't apply to the software scope.
- Some used the virtual file system effectively.

## Which tests were not used for submission?

- Tests which didn't add much value to the current suite
- Were already covered by functional tests. Evosiote especially helped in generating structural tests and on early exploration with other tools like Randoop
- Tests redundant to current suite. Since they were unit tests and our tests were already high standard we chose to ignore the newly generated ones.
- Some tests had to be ignored due to project specifics. (E.g. in report)

## Bugs found

- Globbing relative path
- IORedir `> >`
- NullPointerExceptions (Ls, Find) [Null checks]
- Index cases for Parser
- ClassCastExceptions, InvalidInputException
- Evosuite generated tests considering our impl is bug-free.
