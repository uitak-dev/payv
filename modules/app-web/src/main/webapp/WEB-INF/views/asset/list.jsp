<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>자산 관리</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="settings" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <h1 class="text-base font-semibold">자산 관리</h1>
            <a href="${ctx}/ledger/transactions" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">거래</a>
        </div>
    </header>

    <main class="px-4 pt-4 pv-safe-bottom">
        <c:if test="${not empty created}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">자산이 추가되었습니다.</p>
        </c:if>
        <c:if test="${not empty deactivated}">
            <p class="mb-3 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">자산이 비활성화되었습니다.</p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <div class="space-y-4">
            <section class="pv-card p-4">
                <div class="text-sm font-semibold">자산 추가</div>
                <form method="post" action="${ctx}/asset/assets" class="mt-3 grid gap-3" data-ajax="true">
                    <label>
                        <div class="text-sm font-medium">이름</div>
                        <input name="name" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="예: 카드(신한)"/>
                    </label>
                    <label>
                        <div class="text-sm font-medium">유형</div>
                        <select name="assetType" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                            <option value="CASH">현금</option>
                            <option value="CARD">카드</option>
                            <option value="BANK_ACCOUNT">계좌</option>
                        </select>
                    </label>
                    <button type="submit" class="rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white">추가</button>
                </form>
            </section>

            <section class="pv-card p-4">
                <div class="text-sm font-semibold">자산 목록</div>
                <div class="mt-4 divide-y divide-slate-100">
                    <c:forEach var="asset" items="${assets}">
                        <div class="flex items-center justify-between py-3">
                            <div>
                                <div class="text-sm font-medium">${asset.name}</div>
                                <div class="mt-0.5 text-xs text-slate-500">${asset.assetType}</div>
                            </div>
                            <form method="post" action="${ctx}/asset/assets/${asset.assetId}" data-ajax="true" data-method="DELETE">
                                <button type="submit" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">비활성</button>
                            </form>
                        </div>
                    </c:forEach>
                    <c:if test="${empty assets}">
                        <p class="py-3 text-sm text-slate-500">등록된 자산이 없습니다.</p>
                    </c:if>
                </div>
            </section>
        </div>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
