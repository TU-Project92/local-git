sap.ui.define([
    "sap/ui/core/mvc/Controller",
    "sap/ui/model/json/JSONModel",
    "sap/m/MessageBox",
    "sap/m/MessageToast",
    "sap/ui/core/Fragment"
], function (Controller, JSONModel, MessageBox, MessageToast, Fragment) {
    "use strict";

    return Controller.extend("com.example.project.frontend.frontend.controller.AdminDashboard", {

        onInit: function () {
            var sToken = localStorage.getItem("token");
            var oStoredUser = {};

            try {
                oStoredUser = JSON.parse(localStorage.getItem("user") || "{}");
            } catch (e) {
                oStoredUser = {};
            }

            if (!sToken) {
                this.getOwnerComponent().getRouter().navTo("RouteLogin");
                return;
            }

            if (oStoredUser.systemRole !== "ADMIN") {
                MessageBox.error("You do not have access to this page.");
                this.getOwnerComponent().getRouter().navTo("RouteDashboard");
                return;
            }

            var oModel = new JSONModel({
                currentUser: oStoredUser,
                search: "",
                documents: [],
                users: [],
                selectedUser: {
                    id: null,
                    username: "",
                    firstName: "",
                    lastName: "",
                    fullName: "",
                    email: "",
                    systemRole: "",
                    myInfo: "",
                    active: true
                }
            });

            this.getView().setModel(oModel, "admin");
            this._loadAdminData("");
        },

        _getAuthHeaders: function () {
            return {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + localStorage.getItem("token")
            };
        },

        _handleUnauthorized: function () {
            localStorage.removeItem("token");
            localStorage.removeItem("user");
            this.getOwnerComponent().getRouter().navTo("RouteLogin");
        },

        _mapUser: function (oUser) {
            var sFirstName = oUser.firstName || "";
            var sLastName = oUser.lastName || "";
            var sUsername = oUser.username || "";
            var sFullName = (sFirstName + " " + sLastName).trim() || sUsername;

            return {
                id: oUser.id,
                username: sUsername,
                firstName: sFirstName,
                lastName: sLastName,
                fullName: sFullName,
                email: oUser.email || "",
                systemRole: oUser.systemRole || "",
                myInfo: oUser.myInfo || "",
                active: typeof oUser.active === "boolean" ? oUser.active : true
            };
        },

        _loadAdminData: async function (sSearch) {
            var oModel = this.getView().getModel("admin");
            var sEncodedSearch = encodeURIComponent((sSearch || "").trim());

            try {
                const [oDocumentsResponse, oUsersResponse] = await Promise.all([
                    fetch("http://localhost:8080/api/admin/documents?search=" + sEncodedSearch, {
                        method: "GET",
                        headers: this._getAuthHeaders()
                    }),
                    fetch("http://localhost:8080/api/admin/users/search?search=" + sEncodedSearch, {
                        method: "GET",
                        headers: this._getAuthHeaders()
                    })
                ]);

                if (
                    oDocumentsResponse.status === 401 || oDocumentsResponse.status === 403 ||
                    oUsersResponse.status === 401 || oUsersResponse.status === 403
                ) {
                    this._handleUnauthorized();
                    return;
                }

                const sDocumentsText = await oDocumentsResponse.text();
                const sUsersText = await oUsersResponse.text();

                if (!oDocumentsResponse.ok) {
                    throw new Error(sDocumentsText || "Cannot load admin documents");
                }

                if (!oUsersResponse.ok) {
                    throw new Error(sUsersText || "Cannot load admin users");
                }

                var aDocuments = sDocumentsText ? JSON.parse(sDocumentsText) : [];
                var aUsers = sUsersText ? JSON.parse(sUsersText) : [];

                oModel.setProperty("/documents", Array.isArray(aDocuments) ? aDocuments : []);
                oModel.setProperty("/users", (Array.isArray(aUsers) ? aUsers : []).map(this._mapUser));

            } catch (oError) {
                MessageBox.error("Admin data loading failed: " + oError.message);
            }
        },

        onSearchAdmin: function (oEvent) {
            var sValue = oEvent.getSource().getValue();
            this.getView().getModel("admin").setProperty("/search", sValue);
            this._loadAdminData(sValue);
        },

        onSearchAdminLiveChange: function (oEvent) {
            var sValue = oEvent.getParameter("value");
            this.getView().getModel("admin").setProperty("/search", sValue);
            this._loadAdminData(sValue);
        },

        onOpenDocument: function (oEvent) {
            var oContext = oEvent.getSource().getBindingContext("admin");
            if (!oContext) {
                return;
            }

            var oDocument = oContext.getObject();

            this.getOwnerComponent().getRouter().navTo("RouteDocumentDetails", {
                documentId: String(oDocument.id)
            });
        },

        onDeleteDocument: function (oEvent) {
            var oContext = oEvent.getSource().getBindingContext("admin");
            if (!oContext) {
                return;
            }

            var oDocument = oContext.getObject();
            var that = this;

            MessageBox.confirm(
                "Are you sure you want to delete document \"" + (oDocument.title || "") + "\"?",
                {
                    title: "Delete document",
                    actions: [MessageBox.Action.DELETE, MessageBox.Action.CANCEL],
                    emphasizedAction: MessageBox.Action.DELETE,
                    onClose: async function (sAction) {
                        if (sAction !== MessageBox.Action.DELETE) {
                            return;
                        }

                        try {
                            const oResponse = await fetch("http://localhost:8080/api/admin/documents/" + oDocument.id, {
                                method: "DELETE",
                                headers: that._getAuthHeaders()
                            });

                            const sText = await oResponse.text();

                            if (oResponse.status === 401 || oResponse.status === 403) {
                                that._handleUnauthorized();
                                return;
                            }

                            if (!oResponse.ok) {
                                throw new Error(sText || "Document deletion failed");
                            }

                            MessageToast.show("Document deleted successfully");
                            that._loadAdminData(that.getView().getModel("admin").getProperty("/search"));
                        } catch (oError) {
                            MessageBox.error("Cannot delete document: " + oError.message);
                        }
                    }
                }
            );
        },

        onOpenUserDetails: async function (oEvent) {
            var oContext = oEvent.getSource().getBindingContext("admin");
            if (!oContext) {
                return;
            }

            var oUser = oContext.getObject();

            try {
                const oResponse = await fetch("http://localhost:8080/api/users/" + oUser.id, {
                    method: "GET",
                    headers: this._getAuthHeaders()
                });

                const sText = await oResponse.text();

                if (oResponse.status === 401 || oResponse.status === 403) {
                    this._handleUnauthorized();
                    return;
                }

                if (!oResponse.ok) {
                    throw new Error(sText || "Cannot load user profile");
                }

                var oProfile = sText ? JSON.parse(sText) : {};
                var oMappedUser = this._mapUser(oProfile);

                this.getView().getModel("admin").setProperty("/selectedUser", oMappedUser);

                if (!this._pAdminUserDialog) {
                    this._pAdminUserDialog = Fragment.load({
                        id: this.getView().getId(),
                        name: "com.example.project.frontend.frontend.view.fragments.AdminUserDetailsDialog",
                        controller: this
                    }).then(function (oDialog) {
                        this.getView().addDependent(oDialog);
                        return oDialog;
                    }.bind(this));
                }

                var oDialog = await this._pAdminUserDialog;
                oDialog.open();

            } catch (oError) {
                MessageBox.error("Cannot load user details: " + oError.message);
            }
        },

        onCloseAdminUserDetailsDialog: function () {
            var oDialog = this.byId("adminUserDetailsDialog");
            if (oDialog) {
                oDialog.close();
            }
        },

        onToggleUserActive: async function () {
            var oModel = this.getView().getModel("admin");
            var oSelectedUser = oModel.getProperty("/selectedUser");
            var oCurrentUser = oModel.getProperty("/currentUser");

            if (!oSelectedUser || !oSelectedUser.id) {
                return;
            }

            if (oCurrentUser && oCurrentUser.id === oSelectedUser.id) {
                MessageBox.warning("You cannot deactivate your own admin account.");
                return;
            }

            var bWillDeactivate = !!oSelectedUser.active;
            var sUrl = bWillDeactivate
                ? "http://localhost:8080/api/admin/users/deactivate"
                : "http://localhost:8080/api/admin/users/activate";

            try {
                const oResponse = await fetch(sUrl, {
                    method: "PATCH",
                    headers: this._getAuthHeaders(),
                    body: JSON.stringify({
                        userId: oSelectedUser.id,
                        reason: "Status changed by admin from admin dashboard."
                    })
                });

                const sText = await oResponse.text();

                if (oResponse.status === 401 || oResponse.status === 403) {
                    this._handleUnauthorized();
                    return;
                }

                if (!oResponse.ok) {
                    throw new Error(sText || "User status update failed");
                }

                var oResult = sText ? JSON.parse(sText) : {};
                var bNewActive = !!oResult.active;

                oModel.setProperty("/selectedUser/active", bNewActive);

                var aUsers = oModel.getProperty("/users") || [];
                aUsers = aUsers.map(function (oUser) {
                    if (oUser.id === oSelectedUser.id) {
                        return Object.assign({}, oUser, {
                            active: bNewActive
                        });
                    }
                    return oUser;
                });

                oModel.setProperty("/users", aUsers);

                MessageToast.show(bNewActive ? "User activated successfully" : "User deactivated successfully");

            } catch (oError) {
                MessageBox.error("Cannot change user status: " + oError.message);
            }
        },

        onLogout: function () {
            localStorage.removeItem("token");
            localStorage.removeItem("user");
            MessageToast.show("Logged out successfully");
            this.getOwnerComponent().getRouter().navTo("RouteLogin");
        },

        formatDateTime: function (sValue) {
            if (!sValue) {
                return "";
            }

            var oDate = new Date(sValue);

            if (isNaN(oDate.getTime())) {
                return sValue;
            }

            return oDate.toLocaleString();
        }
    });
});