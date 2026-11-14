package com.zephbyte.scaleddragonfight

import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.server.level.ServerPlayer
//? if >=1.21.11 {
import net.minecraft.server.permissions.Permissions
//?}
import java.net.URI

internal fun CommandSourceStack.isAdmin(): Boolean =
    //? if >=1.21.11 {
    permissions().hasPermission(Permissions.COMMANDS_ADMIN)
    //?} else {
    /*hasPermission(3)
    *///?}

internal fun ServerPlayer.isGamemaster(): Boolean =
    //? if >=1.21.11 {
    permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
    //?} else {
    /*hasPermissions(2)
    *///?}

internal fun openUrl(uri: URI): ClickEvent =
    //? if >=1.21.5 {
    ClickEvent.OpenUrl(uri)
    //?} else {
    /*ClickEvent(ClickEvent.Action.OPEN_URL, uri.toString())
    *///?}

internal fun suggestCommand(command: String): ClickEvent =
    //? if >=1.21.5 {
    ClickEvent.SuggestCommand(command)
    //?} else {
    /*ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)
    *///?}

internal fun showText(text: Component): HoverEvent =
    //? if >=1.21.5 {
    HoverEvent.ShowText(text)
    //?} else {
    /*HoverEvent(HoverEvent.Action.SHOW_TEXT, text)
    *///?}
