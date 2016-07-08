const React = require('react');
const FontAwesome = require('react-fontawesome');

module.exports = React.createClass({

  handleDoubleClick: function() {
    const { type, label } = this.props;
    if (type === 'file') {
      this.props.open(label);
    } else {
      this.props.navigate(label);
    }
  },

  getIcon(type) {
    if (type === 'file') {
      return <FontAwesome name='file-o' className='px1' />
    } else {
      return <FontAwesome name='folder-o' className='px1' />
    }
  },

  render: function() {
    return (
      <div className="directory-item p1" onDoubleClick={this.handleDoubleClick} >
        {this.getIcon(this.props.type)}
        <span>{this.props.label}</span>
      </div>
    );
  }
});
