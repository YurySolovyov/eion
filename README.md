# Eion

Eion is a file manager built with web technologies.

Screenshot:

![Eion Screenshot](https://raw.githubusercontent.com/YurySolovyov/eion/f58b15613f2097ba68b2cd00810e2465e4f47d5b/screenshot.png "Eion")

## Trying it out:

0. Install [`boot`](https://github.com/boot-clj/boot#install)
1. Install [`electron`](https://github.com/electron/electron): `npm i -g electron`
2. Clone the repo
4. Run `boot watch dev-build`
5. Run `boot npm-install`
6. Run `electron target/`

## Project goals

### Project goals:
* run on Windows and Linux
* provide basic file and directory operations
  - Execute
  - Create
  - Copy
  - Move
  - Rename
  - Delete
* basic archive files support:
  - View archive contents
  - Extract contents
  


### Project non-goals:
* compete with other File Managers
* provide a lot of customization points
* support plugins (at least not for 1.0)
* support cloud storage

## License

MIT © Yury Solovyov
