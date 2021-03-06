/*
 * RHQ Management Platform
 * Copyright (C) 2005-2010 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.rhq.enterprise.gui.alert.converter;

import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import org.rhq.core.domain.alert.notification.AlertNotification;

public class AlertNotificationConverter implements Converter {

    private List<AlertNotification> alertNotifications;

    public void setAlertNotifications(List<AlertNotification> alertNotifications) {
        this.alertNotifications = alertNotifications;
    }

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        int notificationId = Integer.parseInt(value);

        for (AlertNotification notification : alertNotifications) {
            if (notificationId == notification.getId()) {
                return notification;
            }
        }

        return null;
    }

    public String getAsString(FacesContext context, UIComponent component, Object notificationObject) {
        AlertNotification notification = (AlertNotification) notificationObject;

        return Integer.toString(notification.getId());
    }

}