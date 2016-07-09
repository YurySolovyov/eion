const { app, BrowserWindow, ipcMain } = require('electron');

let win;

const createWindow = function () {
  win = new BrowserWindow({ width: 800, height: 600 });
  win.maximize();
  win.loadURL(`file://${__dirname}/index.html`);

  win.on('closed', () => {
    win = null;
  });

  ipcMain.on('toggleDevTools', function() {
    win.toggleDevTools();
  });
};

const onWindowAllClosed = function() {
  if (process.platform !== 'darwin') {
    app.quit();
  }
};

const onAppActivate = function() {
  if (win === null) {
    createWindow();
  }
};

app.on('ready', createWindow);
app.on('window-all-closed', onWindowAllClosed);
app.on('activate', onAppActivate);
