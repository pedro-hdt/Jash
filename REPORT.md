# CS4218 Team Project: Shell - QA Report

## Injected bugs

Here is a list of all the bugs that we believe have been injected in the code provided:

### Commit [bb84252](https://github.com/nus-cs4218/cs4218-project-ay1920-s2-2020-team03/commit/bb84252d532ffae18a2cf674bc045e46198af2f2)

* `break` outside loop or switch statement in `ShellImpl` main function, causing a build error. This has been fixed, but no tests were written as the build error will no longer occur

* Inverted boolean logic in `CallComand.evaluate()`

* Missing `break` statements in `ApplicationRunner`

* Uncaught and undeclared exception in `IOUtils.openOutputStream()`

* Missing increment causing infinite loop in `StringUtils.isBlank()`

### Commit [dd06ee3](https://github.com/nus-cs4218/cs4218-project-ay1920-s2-2020-team03/commit/dd06ee3a0de623e63c01c7700206085937ed66cb)

* Inverted boolean logic in `StringUtils.isBlank()`

### Commit [bfe24a0](https://github.com/nus-cs4218/cs4218-project-ay1920-s2-2020-team03/pull/1/commits/bfe24a05f971d3a0a185ab2b7d7a0e2199e0ce45)

* Prevent Shell from exiting after just one command

### Commit []()

* Removed unused variable `currentDirecgory` in `ShellImpl`

* Fixed missing shell prompt

* Broken null check in `IORedirectionHandler`. Found using the PMD rule set.

* Inconsistent new lines at the end of echo command


### Commit []()

* Fixed implementation of LS command. The implementation bug was mentioned in comment in `sg.edu.nus.comp.cs4218.impl.app.LsApplication#buildResult()`

* Performance improvement by using `StringBuilder` in `RegexArgument#globFiles()`

### Commit []()

* Fixed ignored `<` for input redirection in `CommandBuilder`

* Fixed broken check in `IORedirectionHandler.isRedirOperator()`

### Commit []()

* ArrayIndexOutOfBounds exception when calling cd without args. Error message printed is just 0. Created a test case for this but not up to us to fix.


### Commit []()

* SedApplication fix indexing and matching to replace the expected match

* SedApplication write the result by replacing the file's content with result