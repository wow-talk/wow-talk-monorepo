package io.wowtalk.user.service;

import io.wowtalk.user.domain.User;
import io.wowtalk.user.domain.UserId;

public interface UserService {

    User createGuest(String displayName);

    User get(UserId userId);
}
