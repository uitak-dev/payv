<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${mode == 'edit' ? '이체 수정' : '이체 추가'}</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="report" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <a href="${ctx}/ledger/transfers${mode == 'edit' ? '/' : ''}${mode == 'edit' ? transfer.transferId : ''}" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">뒤로</a>
            <h1 class="text-base font-semibold">${mode == 'edit' ? '이체 수정' : '이체 추가'}</h1>
            <span class="w-12"></span>
        </div>
    </header>

    <main class="px-4 pt-4 pb-24">
        <c:if test="${not empty error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <form action="${ctx}${action}" method="post" class="space-y-4" data-ajax="true" data-method="${mode == 'edit' ? 'PUT' : 'POST'}" data-json="${mode == 'edit' ? 'true' : 'false'}">
            <section class="pv-card p-4">
                <div class="grid gap-4">
                    <label class="block">
                        <div class="text-sm font-medium">금액</div>
                        <input name="amount" inputmode="numeric" required type="number" min="1" value="${mode == 'edit' ? transfer.amount : ''}" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="0"/>
                    </label>

                    <label class="block">
                        <div class="text-sm font-medium">날짜</div>
                        <input name="transferDate" type="date" required value="${mode == 'edit' ? transfer.transferDate : today}" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                    </label>

                    <label class="block">
                        <div class="text-sm font-medium">출금 자산</div>
                        <select name="fromAssetId" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                            <option value="">선택</option>
                            <c:forEach var="asset" items="${assets}">
                                <option value="${asset.assetId}" ${mode == 'edit' and asset.assetId == transfer.fromAssetId ? 'selected' : ''}>${asset.name}</option>
                            </c:forEach>
                        </select>
                    </label>

                    <label class="block">
                        <div class="text-sm font-medium">입금 자산</div>
                        <select name="toAssetId" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                            <option value="">선택</option>
                            <c:forEach var="asset" items="${assets}">
                                <option value="${asset.assetId}" ${mode == 'edit' and asset.assetId == transfer.toAssetId ? 'selected' : ''}>${asset.name}</option>
                            </c:forEach>
                        </select>
                        <p class="mt-1 text-xs text-slate-500">출금/입금 자산은 동일할 수 없습니다.</p>
                    </label>

                    <label class="block">
                        <div class="text-sm font-medium">메모</div>
                        <textarea name="memo" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" rows="3" placeholder="메모를 입력하세요">${mode == 'edit' ? transfer.memo : ''}</textarea>
                    </label>
                </div>
            </section>

            <button type="submit" class="w-full rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white">${submitLabel}</button>
        </form>

    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
