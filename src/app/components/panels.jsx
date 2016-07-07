const React = require('react');
const Panel = require('./panel');

module.exports = React.createClass({
  render: function() {
    return (
      <div id="panelsContainer" className="flex">
        <Panel
          {...this.props}
          id="left"
          items={this.props.leftPanel.directoryItems}
        />
        <Panel
          {...this.props}
          id="right"
          items={this.props.rightPanel.directoryItems}
        />
      </div>
    );
  }
});
