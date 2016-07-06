const React = require('react');
const DirectoryItem = require('./directory-item');

module.exports = React.createClass({

  renderItems: function(items) {
    const navigate = this.props.navigate;
    return items.map(function(item, index) {
      return (
        <DirectoryItem
          key={index}
          label={item}
          navigate={navigate} />
      );
    });
  },

  render: function() {
    return (
      <div className="panel flex" id={this.props.id}>
        <DirectoryItem
          key="up"
          label='..'
          navigate={this.props.navigate} />
        {this.renderItems(this.props.items)}
      </div>
    );
  }
});
