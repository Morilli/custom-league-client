package com.hawolt.client.misc;

import com.hawolt.authentication.ICookieSupplier;
import com.hawolt.authentication.LocalCookieSupplier;
import com.hawolt.generic.data.Platform;
import com.hawolt.http.auth.Gateway;
import com.hawolt.virtual.riotclient.instance.CaptchaSupplier;
import com.hawolt.virtual.riotclient.instance.MultiFactorSupplier;

/**
 * Created: 27/07/2023 21:31
 * Author: Twitter @hawolt
 **/

public class ClientConfiguration {
    private final Builder builder;

    public ClientConfiguration(Builder builder) {
        this.builder = builder;
    }

    public MultiFactorSupplier getMultifactorSupplier() {
        return builder.multifactor;
    }

    public String getUsername() {
        return builder.username;
    }

    public String getPassword() {
        return builder.password;
    }

    public boolean getIgnoreSummoner() {
        return builder.ignoreSummoner;
    }

    public boolean getSelfRefresh() {
        return builder.selfRefresh;
    }

    public boolean getComplete() {
        return builder.complete;
    }

    public boolean getMinimal() {
        return builder.minimal;
    }

    public Gateway getGateway() {
        return builder.gateway;
    }

    public Platform getPlatform() {
        return builder.platform;
    }

    public String getRefreshToken() {
        return builder.ec1;
    }

    public ICookieSupplier getCookieSupplier() {
        return builder.supplier;
    }

    public CaptchaSupplier getCaptchaSupplier() {
        return builder.captchaSupplier;
    }

    public static class Builder {
        private Boolean ignoreSummoner, selfRefresh, complete, minimal;
        private MultiFactorSupplier multifactor;
        private CaptchaSupplier captchaSupplier;
        private String username, password, ec1;
        private ICookieSupplier supplier;
        private Platform platform;
        private Gateway gateway;

        public Builder setCaptchaSupplier(CaptchaSupplier captchaSupplier) {
            this.captchaSupplier = captchaSupplier;
            return this;
        }

        public Builder setPlatform(Platform platform) {
            this.platform = platform;
            return this;
        }

        public Builder setRefreshToken(String ec1) {
            this.ec1 = ec1;
            return this;
        }

        public Builder setGateway(Gateway gateway) {
            this.gateway = gateway;
            return this;
        }

        public Builder setMultifactorSupplier(MultiFactorSupplier multifactor) {
            this.multifactor = multifactor;
            return this;
        }

        public Builder setSupplier(ICookieSupplier supplier) {
            this.supplier = supplier;
            return this;
        }

        public Builder setIgnoreSummoner(boolean ignoreSummoner) {
            this.ignoreSummoner = ignoreSummoner;
            return this;
        }

        public Builder setSelfRefresh(boolean selfRefresh) {
            this.selfRefresh = selfRefresh;
            return this;
        }

        public Builder setMinimal(boolean minimal) {
            this.minimal = minimal;
            return this;
        }

        public Builder setComplete(boolean complete) {
            this.complete = complete;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public ClientConfiguration build() throws IncompleteConfigurationException {
            return new ClientConfiguration(this);
        }
    }

    private static ClientConfiguration.Builder getDefault() {
        return new Builder()
                .setSupplier(new LocalCookieSupplier())
                .setIgnoreSummoner(true)
                .setSelfRefresh(true)
                .setMinimal(false)
                .setComplete(true)
                .setGateway(null);
    }

    public static ClientConfiguration getDefault(Platform platform, String token) {
        try {
            return getDefault()
                    .setRefreshToken(token)
                    .setPlatform(platform)
                    .build();
        } catch (IncompleteConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static ClientConfiguration getDefault(String username, String password, MultiFactorSupplier multifactor) {
        try {
            return getDefault()
                    .setMultifactorSupplier(multifactor)
                    .setUsername(username)
                    .setPassword(password)
                    .build();
        } catch (IncompleteConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
