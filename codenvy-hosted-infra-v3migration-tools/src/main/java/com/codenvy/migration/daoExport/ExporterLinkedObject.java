package com.codenvy.migration.daoExport;

import com.codenvy.api.account.server.exception.AccountException;
import com.codenvy.api.account.shared.dto.Account;
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.organization.server.exception.OrganizationException;
import com.codenvy.api.user.server.exception.UserException;
import com.codenvy.api.user.server.exception.UserProfileException;
import com.codenvy.api.user.shared.dto.Profile;
import com.codenvy.api.user.shared.dto.User;
import com.codenvy.api.workspace.server.exception.WorkspaceException;
import com.codenvy.api.workspace.shared.dto.Workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ExporterLinkedObject implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ExporterLinkedObject.class);

    private CountDownLatch  doneSignal;
    private DaoManager      daoManager;
    private User            user;
    private Profile         profile;
    private Account         account;
    private Subscription    subscription;
    private List<Workspace> workspaces;

    public ExporterLinkedObject(CountDownLatch doneSignal, DaoManager daoManager, User user, Profile profile, Account account,
                                Subscription subscription,
                                List<Workspace> workspaces) {
        this.workspaces = workspaces;
        this.account = account;
        this.user = user;
        this.profile = profile;
        this.daoManager = daoManager;
        this.subscription = subscription;
        this.doneSignal = doneSignal;
    }

    @Override
    public void run() {
        try {
            daoManager.addUser(user);
            daoManager.addProfile(profile);

            if (account != null) {
                try {
                    daoManager.addAccount(account);
                } catch (AccountException e) {
                    LOG.error("Error exporting organization " + account.getId(), e);
                }
                if (subscription != null) {
                    try {
                        daoManager.addAccountSubscription(subscription);
                    } catch (AccountException e) {
                        LOG.error("Error exporting subscription " + account.getId(), e);
                    }
                }
            }

            for (Workspace workspace : workspaces) {
                try {
                    daoManager.addWorkspace(workspace);
                } catch (WorkspaceException e) {
                    LOG.error("Error exporting workspace " + workspace.getId(), e);
                }
            }
        } catch (UserException e) {
            LOG.error("Error exporting user " + user.getId(), e);
        } catch (UserProfileException e) {
            LOG.error("Error exporting user's profile " + user.getId(), e);
        }

        doneSignal.countDown();
    }

}
