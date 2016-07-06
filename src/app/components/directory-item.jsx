const React = require('react');

module.exports = React.createClass({

  handleDoubleClick: function() {
    this.props.navigate(this.props.label);
  },

  render: function() {
    return (
      <div className="directory-item p1" onDoubleClick={this.handleDoubleClick} >{this.props.label}</div>
    );
  }
});
