<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>이체 목록</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="ledger" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <h1 class="text-base font-semibold">거래/이체</h1>
            <a href="${ctx}/ledger/transfers/new" class="rounded-xl bg-slate-900 px-3 py-1.5 text-sm font-semibold text-white">이체 추가</a>
        </div>
    </header>

    <main class="px-4 pt-4 pv-safe-bottom">
        <section class="mb-3">
            <div class="inline-flex rounded-full bg-slate-100 p-1">
                <a href="${ctx}/ledger/transactions" class="rounded-full px-4 py-1.5 text-sm text-slate-500">거래 목록</a>
                <a href="${ctx}/ledger/transfers" class="rounded-full bg-white px-4 py-1.5 text-sm font-semibold text-slate-900 shadow-sm">이체 목록</a>
            </div>
        </section>

        <c:if test="${not empty notice.created}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">이체가 생성되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.updated}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">이체가 수정되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.deleted}">
            <p class="mb-3 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">이체가 삭제되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <section class="pv-card p-4">
            <form method="get" action="${ctx}/ledger/transfers" class="grid gap-3">
                <div class="grid grid-cols-2 gap-2">
                    <label>
                        <div class="text-xs text-slate-500">시작일</div>
                        <input type="date" name="from" value="${from}" class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                    </label>
                    <label>
                        <div class="text-xs text-slate-500">종료일</div>
                        <input type="date" name="to" value="${to}" class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                    </label>
                </div>
                <button type="submit" class="rounded-xl border border-slate-200 px-3 py-2 text-sm">조회</button>
            </form>
        </section>

        <section class="mt-4 space-y-3">
            <c:forEach var="item" items="${result.items}">
                <article class="pv-card p-4">
                    <a href="${ctx}/ledger/transfers/${item.transferId}" class="block">
                        <div class="flex items-start justify-between gap-3">
                            <div>
                                <div class="text-xs text-slate-500">${item.transferDate}</div>
                                <div class="mt-1 text-sm font-semibold">${item.fromAssetName} → ${item.toAssetName}</div>
                                <div class="mt-1 text-xs text-slate-500">${item.memo}</div>
                            </div>
                            <div class="text-sm font-semibold text-slate-700">
                                <fmt:formatNumber value="${item.amount}" pattern="#,##0"/>
                            </div>
                        </div>
                    </a>
                </article>
            </c:forEach>
            <c:if test="${empty result.items}">
                <section class="pv-card p-4 text-sm text-slate-500">조회된 이체가 없습니다.</section>
            </c:if>
        </section>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
