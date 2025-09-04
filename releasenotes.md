# Release Notes

## 1.2.3

- fixed code scanning alert #1

## 1.2.2

- fixed code scanning alert #2

## 1.2.1

- added explanation for why the source code of inner classes is not (directly) analysed and compared, see issue #43

## 1.2.0

- added CLI parameter to use DALEQ with either sound rules only, soundy (and sound) rules, or both

## 1.1.1

- fixed minor bug -- assertion in CLI Main

## 1.1.0

- added CLI exit codes
- CLI now has an `-autoopen` option, only if this is used the generated report is opened at the end
- support for `FREM` instruction added
- minor bugfixes
- fixed issue #31 -- support for anonymous inner classes within inner classes, will increase equivalences
- code for evaluation experiments moved to [separate project](https://github.com/binaryeq/daleq-evaluation/)

## 1.0.0

Initial Release
