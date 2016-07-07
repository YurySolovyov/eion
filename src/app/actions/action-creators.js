module.exports = {
  navigate: function(path) {
    return {
      type: 'NAVIGATE',
      path
    };
  },

  open: function(item) {
    return {
      type: 'OPEN',
      item
    };
  },

  activatePanel: function(id) {
    return {
      type: 'ACTIVATE_PANEL',
      id
    };
  }
};
