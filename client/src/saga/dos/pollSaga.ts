import { delay } from 'redux-saga';
import {
    call,
    put,
    select,
    takeLatest,
} from 'redux-saga/effects';

import dosDashboardRefresh from 'corla/action/dos/dashboardRefresh';
import dosFetchAsmState from 'corla/action/dos/fetchAsmState';
import dosFetchContests from 'corla/action/dos/fetchContests';


function* dosPoll() {
    const DOS_POLL_DELAY = 1000 * 5;

    const { dashboard, loggedIn } = yield select();

    if (!loggedIn) { return null; }
    if (dashboard !== 'sos') { return null; }

    yield delay(DOS_POLL_DELAY);

    dosDashboardRefresh();
    dosFetchAsmState();
    dosFetchContests();

    yield put({ type: 'DOS_POLL' });
}


export default function* dosPollSaga() {
    yield takeLatest('DOS_POLL', dosPoll);
}