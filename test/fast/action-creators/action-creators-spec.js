import path from 'path';
import test from 'ava';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import actionCreators from '../../../src/app/actions/action-creators';

const fakeDirectoryService = {
  getDirectoryItems: (dir) => { return Promise.resolve(['foo.exe']); }
};

const middlewares = [
  thunk.withExtraArgument({
    DirectoryService: fakeDirectoryService
  })
];

const mockStore = configureMockStore(middlewares);

let store;
test.beforeEach(t => {
    const state = {
      panels: {
        current: 'left',
        left: {
          currentPath: '',
          directoryItems: []
        }
      }
    };
    store = mockStore(state);
});

test('ACTIVATE_PANEL action', t => {
  store.dispatch(actionCreators.activatePanel('left'));
  t.deepEqual(store.getActions(), [{ type: 'ACTIVATE_PANEL', id: 'left' }]);
});


test('OPEN action', t => {
  store.dispatch(actionCreators.open('/foo/bar'));
  t.deepEqual(store.getActions(), [{ type: 'OPEN', item: '/foo/bar' }]);
});

test('NAVIGATE action', t => {
  const newPath = path.resolve('/foo/bar');
  store.dispatch(actionCreators.navigate(newPath)).then(newItems => {
    t.deepEqual(store.getActions(), [{ type: 'NAVIGATE', path: newPath, items: newItems }]);
  });
});
