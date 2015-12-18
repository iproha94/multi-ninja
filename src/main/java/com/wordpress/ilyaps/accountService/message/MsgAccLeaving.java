package com.wordpress.ilyaps.accountService.message;

import com.wordpress.ilyaps.accountService.AccountService;
import com.wordpress.ilyaps.accountService.UserProfile;
import com.wordpress.ilyaps.frontendService.message.MsgFrnLeft;
import com.wordpress.ilyaps.messageSystem.Address;
import com.wordpress.ilyaps.messageSystem.Message;
import org.jetbrains.annotations.NotNull;

/**
 * Created by ilya on 13.12.15.
 */
public class MsgAccLeaving extends MsgToAccountService {
    @NotNull
    private String sessionId;
    @NotNull
    private String email;

    public MsgAccLeaving(
            @NotNull Address from,
            @NotNull Address to,
            @NotNull String sessionId,
            @NotNull String email
    ) {
        super(from, to);
        this.sessionId = sessionId;
        this.email = email;
    }

    @Override
    protected void exec(AccountService service) {
        final UserProfile profile = service.getAccountServiceDAO().leaving(sessionId);
        final Message msg = new MsgFrnLeft(getTo(), getFrom(), email, sessionId, profile);
        service.sendMessage(msg);
    }
}
