package com.obshenie.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Chat(
    val name: String,
    val preview: String,
    val time: String,
    val mine: Boolean = false,
    val badge: String? = null,
    val muted: Boolean = false,
    val pinned: Boolean = false,
    val online: Boolean = false,
)

private val chats = listOf(
    Chat("Полина", "Скинь фотки с выходных, пожалуйста, те что на пляже. Хочу показать маме, она ещё не видела, как там было красиво и тепло для начала осени", "22:32", pinned = true, online = true),
    Chat("Артур", "Привет, не хочешь встретиться позже? Я как раз буду рядом с центром, могу заехать", "21:44", badge = "1"),
    Chat("Мама", "Позвони, как будешь свободен", "21:10"),
    Chat("Джессика", "Что делаешь на выходных? Мы с девчонками думали выбраться за город, взять палатки и остаться на ночь, если погода позволит, будет супер, присоединяйся", "21:02", online = true),
    Chat("Моника", "Ха-ха, это было очень смешно, я до сих пор не могу перестать смеяться", "19:17", mine = true),
    Chat("Саша", "Голосовое сообщение · 0:42", "18:40", badge = "3", muted = true),
    Chat("Игорь", "Договорились, завтра в десять", "вчера", mine = true),
    Chat("Катя", "Спасибо тебе огромное!", "пн"),
    Chat("Дима", "Посмотрел, всё отлично", "пн", mine = true),
    Chat("Лена", "Созвонимся вечером?", "вс"),
    Chat("Паша", "Ок, до встречи", "сб"),
)

private val tabTitles = listOf("Контакты", "Общение", "Профиль")

@Composable
fun ChatsScreen(onToggleTheme: (Offset) -> Unit) {
    val p = LocalPalette.current
    var tab by rememberSaveable { mutableIntStateOf(1) }
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var readAll by rememberSaveable { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(p.bg)
    ) {
        Column(Modifier.fillMaxSize()) {
            Header(
                title = tabTitles[tab],
                menuOpen = menuOpen,
                onMenu = { menuOpen = true },
                onToggleTheme = onToggleTheme
            )

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (tab == 1) {
                    LazyColumn(Modifier.fillMaxSize()) {
                        itemsIndexed(chats) { index, chat -> ChatRow(chat, index, showBadge = !readAll) }
                    }
                }
            }

            NavBar(selected = tab, onSelect = { tab = it })
        }

        // меню по трём точкам поверх всего экрана
        HeaderMenu(
            open = menuOpen,
            onOpen = { menuOpen = it },
            onNewChat = { },
            onReadAll = { readAll = true }
        )
    }
}

@Composable
private fun Header(
    title: String,
    menuOpen: Boolean,
    onMenu: () -> Unit,
    onToggleTheme: (Offset) -> Unit,
) {
    val p = LocalPalette.current
    var themeBtn by remember { mutableStateOf(Offset.Zero) }
    // точки гаснут, пока из них вырастает меню, и возвращаются, когда оно свернулось обратно
    val dotsAlpha by animateFloatAsState(
        if (!menuOpen) 1f else 0f,
        tween(150, delayMillis = if (!menuOpen) 280 else 0),
        label = "dots"
    )

    Box(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 6.dp)
            .height(56.dp)
            .padding(horizontal = 8.dp)
    ) {
        IconBtn(
            Modifier.align(Alignment.CenterStart),
            description = "Сменить тему",
            onClick = { onToggleTheme(themeBtn) },
            onCenter = { themeBtn = it }
        ) {
            // в тёмной теме солнце (переключить на светлую), в светлой луна
            ThemeIcon(p.dark, p.sub, Modifier.size(24.dp))
        }
        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            color = p.txt,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        IconBtn(
            Modifier
                .align(Alignment.CenterEnd)
                .graphicsLayer { alpha = dotsAlpha },
            description = "Ещё",
            enabled = !menuOpen,
            onClick = onMenu
        ) {
            PathIcon(Glyphs.DOTS, p.sub, Modifier.size(24.dp))
        }
    }
}

@Composable
private fun IconBtn(
    modifier: Modifier,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    onCenter: ((Offset) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .size(48.dp)
            .onGloballyPositioned {
                onCenter?.invoke(it.positionInWindow() + Offset(it.size.width / 2f, it.size.height / 2f))
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun ChatRow(c: Chat, index: Int, showBadge: Boolean) {
    val p = LocalPalette.current
    val (avBg, avFg) = p.avatars[index % p.avatars.size]

    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 86.dp)
            .background(if (c.pinned) p.bar else Color.Transparent)
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.size(62.dp)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(avBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = c.name.first().toString(),
                    color = avFg,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (c.online) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 2.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(p.online)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = c.name,
                color = p.txt,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildAnnotatedString {
                    if (c.mine) {
                        withStyle(SpanStyle(color = p.gt)) { append("Вы: ") }
                    }
                    append(c.preview)
                },
                modifier = Modifier.padding(top = 1.dp),
                color = p.sub,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(horizontalAlignment = Alignment.End) {
            Row(
                Modifier.height(22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (c.mine) {
                    PathIcon(
                        Glyphs.CHECK, p.gt, Modifier.size(16.dp, 11.dp),
                        box = 16f, strokeWidth = 1.6f
                    )
                }
                Text(text = c.time, color = p.sub, fontSize = 13.sp, lineHeight = 22.sp)
            }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.height(22.dp), contentAlignment = Alignment.CenterEnd) {
                when {
                    c.pinned -> PathIcon(Glyphs.PIN, p.sub, Modifier.size(18.dp))
                    c.badge != null && showBadge -> Box(
                        Modifier
                            .defaultMinSize(minWidth = 22.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (c.muted) p.badgeMuted else p.badge)
                            .padding(horizontal = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = c.badge,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavBar(selected: Int, onSelect: (Int) -> Unit) {
    val p = LocalPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(p.bar)
            .navigationBarsPadding()
            .height(72.dp)
            .padding(top = 12.dp, start = 8.dp, end = 8.dp)
    ) {
        for (i in 0..2) {
            val sel = i == selected
            val color = if (sel) p.gt else p.sub
            Column(
                Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(i) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val icon = Modifier.size(26.dp)
                when (i) {
                    0 -> PathIcon(Glyphs.CONTACTS, color, icon, strokeWidth = 1.8f)
                    1 -> if (sel) PathIcon(Glyphs.CHAT, color, icon)
                    else PathIcon(Glyphs.CHAT, color, icon, strokeWidth = 1.8f)
                    else -> PathIcon(Glyphs.PROFILE, color, icon, strokeWidth = 1.8f)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = tabTitles[i],
                    color = color,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
