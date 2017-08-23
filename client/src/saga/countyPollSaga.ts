import { delay } from 'redux-saga';
import {
    call,
    put,
    select,
    takeLatest,
} from 'redux-saga/effects';

import countyDashboardRefresh from '../action/countyDashboardRefresh';
import countyFetchContests from '../action/countyFetchContests';
import fetchAuditBoardAsmState from '../action/fetchAuditBoardAsmState';
import fetchCountyAsmState from '../action/fetchCountyAsmState';


function* countyPoll() {
    const COUNTY_POLL_DELAY = 1000 * 5;

    const { county, dashboard, loggedIn } = yield select();

    if (!loggedIn) { return null; }
    if (dashboard !== 'county') { return null; }

    yield delay(COUNTY_POLL_DELAY);

    countyDashboardRefresh();
    fetchAuditBoardAsmState();
    fetchCountyAsmState();

    if (county && county.id) {
        countyFetchContests(county.id);
    }

    yield put({ type: 'COUNTY_POLL' });
}


export default function* countyPollSaga() {
    yield takeLatest('COUNTY_POLL', countyPoll);
}
