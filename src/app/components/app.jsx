const React = require('react');
const Panel = require('./panel');

module.exports = React.createClass({
  render: function() {
    return (
      <div id="container">
        <div id="panelsContainer" className="flex">
          <Panel id="leftPanel" />
          <Panel id="rightPanel" />
        </div>
      </div>
    );
  }
});
