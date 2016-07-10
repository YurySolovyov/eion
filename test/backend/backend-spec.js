import path from 'path';
import test from 'ava';
import { Application } from 'spectron';

const pack = require('../../package.json');
const exePath = process.platform === 'win32' ? 'eion-win32-x64/eion.exe' : 'eion-linux-x64/eion';

test.beforeEach(t => {
  t.context.app = new Application({
    path: path.resolve('../../out', pack.version, exePath)
  });

  return t.context.app.start();
});

test.afterEach(t => {
  return t.context.app.stop();
});

test('app starts sucessfully', async t => {
  const app = t.context.app;
  await app.client.waitUntilWindowLoaded();

  const win = app.browserWindow;
  t.is(await app.client.getWindowCount(), 1);
  t.false(await win.isMinimized());
  t.false(await win.isDevToolsOpened());
  t.true(await win.isVisible());
  t.true(await win.isFocused());

  const {width, height} = await win.getBounds();
  t.true(width > 0);
  t.true(height > 0);
});
