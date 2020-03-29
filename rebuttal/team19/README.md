# Guide to run tests

1. Put the `hackathon` folder into your project folder.

  IMPORTANT: It should be put at the same level as `src` folder.

2. Open IntelliJ, mark the hackathon folder as a test folder

3. On the left panel, right click to the `hack` folder, click `Run 'Tests in hack'`

# Guide to debug with our tests

1. For each failed test, find the test id (e.g. `"exit/01"`)

2. Find two files: `<test-id>.in.txt` and `<test-id>.ok.txt`.

The syntax of the `*.in.txt` has some new features:

1. All the lines starting with `#` are comments. They will be ignored.

2. `__mkdir <path>`: to create a folder.

3. `__touch <path>`: to create an empty file.

4. `__append <line>`: add `line` to the *last touched* file.

See `SaferShellImpl.runSpecialCommand` for more details.
