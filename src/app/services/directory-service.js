const path = __non_webpack_require__('path');
const fs = __non_webpack_require__('fs');
const app = __non_webpack_require__('electron').remote.app;

let currentPath = path.dirname(app.getPath('exe'));

module.exports = {
  getDirectoryItems: function() {
    return fs.readdirSync(currentPath);
  }
};
