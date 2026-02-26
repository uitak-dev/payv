<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>알림함</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="settings" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <a href="${ctx}/settings" class="inline-flex items-center rounded-lg border border-slate-200 px-3 py-1.5 text-sm">설정</a>
            <h1 class="text-base font-semibold">알림함</h1>
            <span class="inline-flex items-center rounded-lg border border-slate-200 px-2.5 py-1.5 text-xs text-slate-600">
                안읽음 ${unreadCount}
            </span>
        </div>
    </header>

    <main class="px-4 pt-4 pb-24">
        <c:if test="${notice.read == true}">
            <div class="mb-3 rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">알림을 읽음 처리했습니다.</div>
        </c:if>
        <c:if test="${notice.readAll == true}">
            <div class="mb-3 rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">모든 알림을 읽음 처리했습니다.</div>
        </c:if>

        <div class="mb-3 flex items-center justify-end">
            <form action="${ctx}/notifications/read-all?page=${result.page}&size=${result.size}" method="post" data-ajax="true" data-method="PUT">
                <button type="submit" class="rounded-xl border border-slate-200 px-3 py-2 text-xs font-semibold hover:bg-slate-100">전체 읽음</button>
            </form>
        </div>

        <section class="space-y-3">
            <c:forEach var="item" items="${result.items}">
                <article class="rounded-2xl border ${item.read ? 'border-slate-200 bg-white' : 'border-blue-200 bg-blue-50/40'} p-4">
                    <div class="flex items-start justify-between gap-3">
                        <div>
                            <div class="text-sm font-semibold">${item.title}</div>
                            <div class="mt-1 text-xs text-slate-600">${item.message}</div>
                            <div class="mt-2 text-xs text-slate-500">
                                ${item.type} · ${item.createdAt}
                            </div>
                        </div>
                        <c:if test="${not item.read}">
                            <form action="${ctx}/notifications/${item.notificationId}?page=${result.page}&size=${result.size}" method="post" data-ajax="true" data-method="PUT">
                                <button type="submit" class="rounded-xl border border-slate-200 px-2.5 py-1.5 text-xs font-semibold hover:bg-slate-100">읽음</button>
                            </form>
                        </c:if>
                    </div>
                </article>
            </c:forEach>
            <c:if test="${empty result.items}">
                <article class="rounded-2xl border border-slate-200 bg-white p-6 text-center text-sm text-slate-500">
                    표시할 알림이 없습니다.
                </article>
            </c:if>
        </section>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
