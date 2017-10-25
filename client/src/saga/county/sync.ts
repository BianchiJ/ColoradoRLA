import * as _ from 'lodash';

import { all, select, takeLatest } from 'redux-saga/effects';

import * as config from 'corla/config';

import createPollSaga from 'corla/saga/createPollSaga';

import dashboardRefresh from 'corla/action/county/dashboardRefresh';
import fetchAuditBoardASMState from 'corla/action/county/fetchAuditBoardASMState';
import fetchContests from 'corla/action/county/fetchContests';
import fetchCountyASMState from 'corla/action/county/fetchCountyASMState';


const COUNTY_POLL_DELAY = config.pollDelay;

function* auditPoll() {
    const { county } = yield select();

    const currentState = _.get(county, 'asm.auditBoard.currentState');
    const shouldSync = currentState === 'WAITING_FOR_ROUND_START'
        || currentState === 'WAITING_FOR_ROUND_START_NO_AUDIT_BOARD';

    if (shouldSync) {
        dashboardRefresh();
        fetchAuditBoardASMState();
        fetchCountyASMState();
    }
}

const auditPollSaga = createPollSaga(
    [auditPoll],
    'COUNTY_AUDIT_POLL_START',
    'COUNTY_AUDIT_POLL_STOP',
    () => COUNTY_POLL_DELAY,
);

function* boardSignInSaga() {
    yield takeLatest('COUNTY_BOARD_SIGN_IN_SYNC', () => {
        dashboardRefresh();
        fetchAuditBoardASMState();
        fetchCountyASMState();
    });
}

function* dashboardPoll() {
    dashboardRefresh();
    fetchAuditBoardASMState();
    fetchCountyASMState();

    const { county } = yield select();

    if (county && county.id) {
        fetchContests(county.id);
    }
}

const dashboardPollSaga = createPollSaga(
    [dashboardPoll],
    'COUNTY_DASHBOARD_POLL_START',
    'COUNTY_DASHBOARD_POLL_STOP',
    () => COUNTY_POLL_DELAY,
);


export default function* pollSaga() {
    yield all([
        auditPollSaga(),
        boardSignInSaga(),
        dashboardPollSaga(),
    ]);
}
