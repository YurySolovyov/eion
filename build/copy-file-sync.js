const fs = require('fs');

module.exports = function copyFileSync(from, to) {
  const contents = fs.readFileSync(from)
  fs.writeFileSync(to, contents);
};
