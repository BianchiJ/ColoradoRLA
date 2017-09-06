import * as React from 'react';

import corlaDate from 'corla/date';

import Nav from '../Nav';

import ElectionDateForm from './ElectionDateForm';
import ElectionTypeForm from './ElectionTypeForm';
import RiskLimitForm from './RiskLimitForm';

import setElectionInfo from 'corla/action/dos/setElectionInfo';
import setRiskLimit from 'corla/action/dos/setRiskLimit';

import { timezone } from 'corla/config';
import * as format from 'corla/format';


const Breadcrumb = () => (
    <ul className='pt-breadcrumbs'>
        <li>
            <a className='pt-breadcrumb' href='/sos'>
                SoS
            </a>
        </li>
        <li>
            <a className='pt-breadcrumb pt-breadcrumb-current'>
                Audit Admin
            </a>
        </li>
    </ul>
);

function round(val: number, digits: number) {
    const factor = Math.pow(10, digits);
    return Math.round(val * factor) / factor;
}

const ReadonlyRiskLimit = ({ riskLimit }: any) => {
    const riskLimitPercent = round(riskLimit * 100, 2);

    return (
        <div className='pt-card'>
            <h4>Risk limit set.</h4>
            <div>The risk limit is set at: { riskLimitPercent }%</div>
        </div>
    );
};

const NextButton = (props: any) => {
    const { nextPage } = props;

    return (
        <button onClick={ nextPage } className='pt-button pt-intent-primary'>
            Next
        </button>
    );
};

const SaveButton = (props: any) => {
    const { disabled, forms, riskLimit } = props;

    const buttonClick = () => {
        const { date } = forms.electionDateForm;
        const { type } = forms.electionTypeForm;

        if (date && type) {
            setElectionInfo(corlaDate.parse(date), type);
        }

        if (!riskLimit) {
            const { comparisonLimit } = forms.riskLimit;

            setRiskLimit(comparisonLimit);
        }
    };

    return (
        <button
            disabled={ disabled }
            onClick={ buttonClick }
            className='pt-button pt-intent-primary'>
            Save
        </button>
    );
};

const ReadOnlyPage = (props: any) => {
    const { election, nextPage, riskLimit } = props;

    const electionDate = corlaDate.format(election.date);
    const electionType = format.electionType(election.type);

    return (
        <div>
            <Nav />
            <Breadcrumb />

            <h2>Administer an Audit</h2>

            <div className='pt-card'>
                <h3>Election Info</h3>
                <div>Election Date: { electionDate }</div>
                <div>Election Type: { electionType }</div>
            </div>

            <div className='pt-card'>
                <h3>Risk Limit</h3>
                <ReadonlyRiskLimit riskLimit={ riskLimit } />
            </div>
            <NextButton nextPage={ nextPage } />
        </div>
    );
};

const AuditPage = (props: any) => {
    const { election, formValid, nextPage, riskLimit, setFormValid } = props;

    const electionAndRiskLimitSet = riskLimit
                                 && election
                                 && election.date
                                 && election.type;

    if (electionAndRiskLimitSet) {
        return (
            <ReadOnlyPage
                election={ election }
                nextPage={ nextPage }
                riskLimit={ riskLimit } />
        );
    }

    const forms: any = {};

    const disableButton = !formValid;

    return (
        <div>
            <Nav />
            <Breadcrumb />

            <h2>Administer an Audit</h2>

            <div className='pt-card'>
                <h3>Election Info</h3>
                <div>Enter the date the election will take place, and the type of election.</div>
                <ElectionDateForm forms={ forms } />
                <ElectionTypeForm forms={ forms } setFormValid={ setFormValid } />
            </div>

            <div className='pt-card'>
                <h3>Risk Limit</h3>
                <div>
                    Enter the risk limit for comparison audits as a percentage.
                </div>
                <RiskLimitForm forms={ forms } riskLimit={ riskLimit } />
                <div className='pt-card'>
                    <span className='pt-icon pt-intent-warning pt-icon-warning-sign' />
                    <span> </span>
                    Once entered, this risk limit cannot be modified.
                </div>
                <SaveButton
                    disabled={ disableButton }
                    forms={ forms}
                    riskLimit={ riskLimit } />
            </div>
        </div>
    );
};


export default AuditPage;
