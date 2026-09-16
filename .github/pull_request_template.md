## What
<!-- One sentence. What does this PR do? -->

## Why
<!-- Which requirement / issue? Why now? -->
Closes #

## Changes
- 

## How to test
1. Build & run
   - macOS / Linux: `javac -d bin $(find src -name "*.java") && java -cp bin:res engine.Core`
   - Windows (PowerShell): `javac -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName; java -cp "bin;res" engine.Core`
2. 

## Screenshots
<!-- Required for anything visible on screen -->

## Checklist
- [ ] Branch is `<team>/<feature>` and based on latest `main`
- [ ] One feature per PR, under ~400 changed lines
- [ ] CI is green
- [ ] Touches `DrawManager` / `Core` / `GameScreen`? → explained above
- [ ] I ran the game after my change
- [ ] Upstream PR? → title is `type(requirement): subject`; if it depends on another PR, `Depends on: #N` is the first line of this description
