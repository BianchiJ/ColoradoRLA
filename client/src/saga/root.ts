import { all } from 'redux-saga/effects';

import * as config from '../config';

import ballotNotFoundOkSaga from './ballotNotFoundOkSaga';
import countyDashboardRefreshOkSaga from './countyDashboardRefreshOkSaga';
import countyLoginSaga from './countyLoginSaga';
import countyPollSaga from './countyPollSaga';
import debugSaga from './debugSaga';
import dosLoginSaga from './dosLoginSaga';
import dosPollSaga from './dosPollSaga';
import fileUploadSaga from './fileUploadSaga';
import logoutSaga from './logoutSaga';
import uploadAcvrOkSaga from './uploadAcvrOkSaga';


export default function* rootSaga() {
    const sagas = [
        ballotNotFoundOkSaga(),
        countyLoginSaga(),
        countyPollSaga(),
        countyDashboardRefreshOkSaga(),
        dosLoginSaga(),
        dosPollSaga(),
        fileUploadSaga(),
        logoutSaga(),
        uploadAcvrOkSaga(),
    ];

    if (config.debug) {
        sagas.push(debugSaga());
    }

    yield all(sagas);
}
