/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vincent Zhang/PhoenixLAB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package co.phoenixlab.discord.api;

import co.phoenixlab.discord.api.entities.TokenResponse;
import co.phoenixlab.discord.api.impl.WahrDiscordApiImpl;
import com.codahale.metrics.JmxReporter;

import java.util.Scanner;

public class DebugMain {

    public static final String USER_AGENT = "DiscordBot (https://github.com/vincentzhang96/WahrBot, 1)";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your email, or enter TOKEN <token> to use an existing token:");
        String emailOrToken = scanner.nextLine();
        WahrDiscordApi api;
        if (emailOrToken.startsWith("TOKEN ")) {
            api = new WahrDiscordApiImpl(USER_AGENT, "test", emailOrToken.substring("TOKEN ".length()));
        } else {
            System.out.print("Enter your password: ");
            String pass = scanner.nextLine();
            api = new WahrDiscordApiImpl(USER_AGENT, "test");
            TokenResponse tokenResponse = api.getAndSetToken(emailOrToken, pass);
        }
        JmxReporter reporter = JmxReporter.
                forRegistry(api.getMetrics()).
                build();
        reporter.start();
        api.open();
    }

}
