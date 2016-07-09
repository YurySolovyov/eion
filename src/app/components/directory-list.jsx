const React = require('react');
const DirectoryItem = require('./directory-item');

module.exports = React.createClass({
  renderItems: function(items, type) {
    const navigate = this.props.navigate;
    return items.map((item, index) => {
      return this.renderItem(index, item.name, type);
    });
  },

  renderItem: function(key, label, type) {
    return (
      <DirectoryItem
        key={key}
        label={label}
        type={type}
        navigate={this.props.navigate}
        open={this.props.open} />
    );
  },

  render: function() {
    return (
      <div className="directory-list mx2">
        {this.renderItem('up', '..', 'dir')}
        {this.renderItems(this.props.items.directories, 'dir')}
        {this.renderItems(this.props.items.files, 'file')}
      </div>
    );
  }
});
