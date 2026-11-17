(function () {
    "use strict";

    var accessToken = null;
    var canApprove = false;

    function $(id) {
        return document.getElementById(id);
    }

    function correlationId() {
        return window.crypto && window.crypto.randomUUID
            ? window.crypto.randomUUID()
            : "web-" + Date.now() + "-" + Math.random().toString(16).slice(2);
    }

    function api(method, path, body) {
        var headers = { "X-Correlation-Id": correlationId() };
        if (body !== undefined) {
            headers["Content-Type"] = "application/json";
        }
        if (accessToken) {
            headers.Authorization = "Bearer " + accessToken;
        }
        return fetch(path, {
            method: method,
            headers: headers,
            body: body === undefined ? undefined : JSON.stringify(body)
        }).then(function (res) {
            return res.text().then(function (text) {
                var data = null;
                try {
                    data = text ? JSON.parse(text) : null;
                } catch (e) {
                    data = null;
                }
                return { status: res.status, data: data };
            });
        });
    }

    function show(title, response, extra) {
        var line = title + ": HTTP " + response.status;
        var failed = response.status >= 400;
        if (failed && response.data && response.data.code) {
            line += "\ncode: " + response.data.code
                + "\nmessage: " + response.data.message
                + "\ncorrelationId: " + response.data.correlationId;
        } else if (!failed && response.data && response.data.status) {
            line += "\nstatus: " + response.data.status;
        }
        if (extra) {
            line += "\n" + extra;
        }
        var box = $("last-response");
        box.textContent = line;
        box.className = failed ? "err" : "ok";
    }

    function networkError(error) {
        var box = $("last-response");
        box.textContent = "Network error: " + error.message;
        box.className = "err";
    }

    function roleLabel(authorities) {
        return (authorities || []).map(function (authority) {
            var name = authority.replace(/^ROLE_/, "");
            if (name === "TEACHER") {
                return "TEACHER (senior)";
            }
            if (name === "STUDENT") {
                return "STUDENT (junior)";
            }
            return name;
        }).join(", ");
    }

    function setSession(me, expiresIn) {
        var signedIn = !!me;
        canApprove = signedIn && (me.authorities || []).indexOf("ROLE_TEACHER") !== -1;
        $("login-form").closest("section").hidden = signedIn;
        $("session-heading").parentElement.hidden = !signedIn;
        $("assignments-heading").parentElement.hidden = !signedIn;
        $("locked-action-heading").hidden = !canApprove;
        if (signedIn) {
            $("session-username").textContent = me.username;
            $("session-role").textContent = roleLabel(me.authorities);
            $("token-lifetime").textContent = expiresIn + " seconds ("
                + Math.round(expiresIn / 60) + " minutes)";
        } else {
            canApprove = false;
            $("session-username").textContent = "****";
            $("session-role").textContent = "****";
            $("token-lifetime").textContent = "";
        }
    }

    function renderRows(rows) {
        var body = $("assignments-body");
        body.replaceChildren();
        rows.forEach(function (row) {
            var tr = document.createElement("tr");
            [row.businessKey, row.title, row.status].forEach(function (text) {
                var td = document.createElement("td");
                td.textContent = text;
                tr.appendChild(td);
            });
            if (canApprove) {
                var action = document.createElement("td");
                var button = document.createElement("button");
                button.type = "button";
                button.className = "lock";
                button.textContent = "Approve";
                button.addEventListener("click", function () {
                    approve(row);
                });
                action.appendChild(button);
                tr.appendChild(action);
            }
            body.appendChild(tr);
        });
    }

    function readAll() {
        return api("GET", "/api/v1/assignments").then(function (response) {
            var count = response.status === 200 && Array.isArray(response.data)
                ? response.data.length + " assignment(s)"
                : null;
            show("Read all", response, count);
            renderRows(response.status === 200 && Array.isArray(response.data) ? response.data : []);
        }).catch(networkError);
    }

    function approve(row) {
        api("POST", "/api/v1/assignments/" + encodeURIComponent(row.id) + "/approve")
            .then(function (response) {
                if (response.status === 200) {
                    show("Approve " + row.businessKey, response, "new status: " + response.data.status);
                    return readAll();
                }
                if (response.status === 403) {
                    return api("GET", "/api/v1/assignments/" + encodeURIComponent(row.id))
                        .then(function (again) {
                            var info = again.status === 200
                                ? "Row did not change: status is still " + again.data.status
                                : "Row re-read: HTTP " + again.status;
                            show("Approve " + row.businessKey, response, info);
                        });
                }
                show("Approve " + row.businessKey, response);
            }).catch(networkError);
    }

    $("login-form").addEventListener("submit", function (event) {
        event.preventDefault();
        var body = {
            username: $("username").value,
            password: $("password").value
        };
        api("POST", "/api/v1/auth/login", body).then(function (response) {
            $("password").value = "";
            if (response.status !== 200) {
                accessToken = null;
                setSession(null);
                show("Login", response);
                return;
            }
            accessToken = response.data.accessToken;
            var expiresIn = response.data.expiresIn;
            return api("GET", "/api/me").then(function (me) {
                if (me.status === 200) {
                    setSession(me.data, expiresIn);
                    renderRows([]);
                    show("Login", { status: 200, data: null },
                        "Signed in. The token is kept in memory only.");
                } else {
                    accessToken = null;
                    setSession(null);
                    show("GET /api/me", me);
                }
            });
        }).catch(networkError);
    });

    $("create-assignment-form").addEventListener("submit", function (event) {
        event.preventDefault();
        api("POST", "/api/v1/assignments", {
            businessKey: $("business-key").value,
            title: $("assignment-title").value
        }).then(function (response) {
            show("Create", response,
                response.status === 201 ? "key: " + response.data.businessKey : null);
            if (response.status === 201) {
                return readAll();
            }
        }).catch(networkError);
    });

    $("read-all-button").addEventListener("click", readAll);
    $("logout-button").addEventListener("click", function () {
        accessToken = null;
        setSession(null);
        renderRows([]);
        var box = $("last-response");
        box.textContent = "Logged out. The next API call will have no token.";
        box.className = "";
    });

    setSession(null);
})();
