<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>이체 상세</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="ledger" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <a href="${ctx}/ledger/transfers" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">목록</a>
            <h1 class="text-base font-semibold">이체 상세</h1>
            <a href="${ctx}/ledger/transfers/${transfer.transferId}/edit" class="rounded-lg bg-slate-900 px-3 py-1.5 text-sm text-white">수정</a>
        </div>
    </header>

    <main class="px-4 pt-4 pv-safe-bottom">
        <c:if test="${not empty notice.created}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">이체가 생성되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.updated}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">이체가 수정되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <section class="pv-card p-4">
            <div class="flex items-start justify-between gap-3">
                <div>
                    <div class="text-xs text-slate-500">${transfer.transferDate}</div>
                    <div class="mt-1 text-lg font-semibold">${transfer.fromAssetName} → ${transfer.toAssetName}</div>
                    <div class="mt-1 text-sm text-slate-600">${transfer.memo}</div>
                </div>
                <div class="text-lg font-semibold text-slate-700">
                    <fmt:formatNumber value="${transfer.amount}" pattern="#,##0"/>
                </div>
            </div>

            <div class="mt-4 grid gap-2 text-sm">
                <div class="flex justify-between"><span class="text-slate-500">출금 자산</span><span>${transfer.fromAssetName}</span></div>
                <div class="flex justify-between"><span class="text-slate-500">입금 자산</span><span>${transfer.toAssetName}</span></div>
                <div class="flex justify-between"><span class="text-slate-500">메모</span><span>${transfer.memo}</span></div>
            </div>

            <form method="post" action="${ctx}/ledger/transfers/${transfer.transferId}" class="mt-4" data-ajax="true" data-method="DELETE">
                <button type="submit" class="w-full rounded-xl border border-red-200 px-4 py-3 text-sm font-semibold text-red-700">삭제</button>
            </form>
        </section>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
