<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>리포트</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="report" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <h1 class="text-base font-semibold">리포트</h1>
            <form method="get" action="${ctx}/reports" class="inline-flex items-center gap-2">
                <input type="month" name="month" value="${selectedMonth}" class="rounded-lg border border-slate-200 px-3 py-2 text-sm"/>
                <button type="submit" class="rounded-lg border border-slate-200 px-3 py-2 text-sm">조회</button>
            </form>
        </div>
    </header>

    <main class="px-4 pt-4 pv-safe-bottom">
        <div class="space-y-4">
            <section class="pv-card p-4">
                <div class="flex items-center justify-between">
                    <div class="text-sm font-semibold">월간 리포트</div>
                    <div class="rounded-xl border border-slate-200 px-3 py-2 text-sm">${selectedMonth}</div>
                </div>
                <div class="mt-4 grid grid-cols-2 gap-3">
                    <div class="rounded-xl border border-slate-200 p-3">
                        <div class="text-xs text-slate-500">총지출</div>
                        <div class="mt-1 text-lg font-semibold">
                            <fmt:formatNumber value="${report.totalExpense}" pattern="#,##0"/>
                        </div>
                    </div>
                    <div class="rounded-xl border border-slate-200 p-3">
                        <div class="text-xs text-slate-500">총수입</div>
                        <div class="mt-1 text-lg font-semibold">
                            <fmt:formatNumber value="${report.totalIncome}" pattern="#,##0"/>
                        </div>
                    </div>
                    <div class="rounded-xl border border-slate-200 p-3">
                        <div class="text-xs text-slate-500">순현금흐름</div>
                        <div class="mt-1 text-lg font-semibold">
                            <fmt:formatNumber value="${report.netAmount}" pattern="#,##0"/>
                        </div>
                    </div>
                    <div class="rounded-xl border border-slate-200 p-3">
                        <div class="text-xs text-slate-500">예산 사용률</div>
                        <div class="mt-1 text-lg font-semibold">${report.budgetUsageRate}%</div>
                    </div>
                </div>
                <div class="mt-3 text-xs text-slate-500">월간 집계는 거래일 기준으로 계산됩니다.</div>
            </section>

            <section class="pv-card p-4">
                <div class="flex items-center justify-between">
                    <div class="text-sm font-semibold">자산별 지출 요약</div>
                    <span class="pv-badge"><span class="pv-dot"></span>지출 기준</span>
                </div>
                <div class="mt-3 space-y-4">
                    <c:forEach var="item" items="${report.assetExpenseSummary}">
                        <div>
                            <div class="flex justify-between text-sm">
                                <span>${item.name}</span>
                                <span class="pv-amount negative">-<fmt:formatNumber value="${item.amount}" pattern="#,##0"/></span>
                            </div>
                            <div class="mt-2 pv-bar" style="--w: ${item.usagePercent > 100 ? 100 : item.usagePercent}%"><span></span></div>
                            <div class="mt-1 text-xs text-slate-500">${item.usagePercent}%</div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty report.assetExpenseSummary}">
                        <div class="text-sm text-slate-500">지출 데이터가 없습니다.</div>
                    </c:if>
                </div>
            </section>

            <section class="pv-card p-4">
                <div class="flex items-center justify-between">
                    <div class="text-sm font-semibold">카테고리 요약(1단계)</div>
                    <a href="${ctx}/ledger/transactions" class="rounded-xl border border-slate-200 px-3 py-2 text-sm">원장 보기</a>
                </div>
                <div class="mt-3 divide-y divide-slate-100">
                    <c:forEach var="item" items="${report.categoryExpenseSummary}">
                        <div class="flex justify-between py-3 text-sm">
                            <span>${item.name}</span>
                            <span class="pv-amount negative">-<fmt:formatNumber value="${item.amount}" pattern="#,##0"/></span>
                        </div>
                    </c:forEach>
                    <c:if test="${empty report.categoryExpenseSummary}">
                        <div class="py-3 text-sm text-slate-500">카테고리별 지출 데이터가 없습니다.</div>
                    </c:if>
                </div>
            </section>

            <section class="pv-card p-4">
                <div class="text-sm font-semibold">태그별 지출 합계</div>
                <div class="mt-3 flex flex-wrap gap-2">
                    <c:forEach var="item" items="${report.tagExpenseSummary}">
                        <span class="rounded-full border border-slate-200 px-3 py-1.5 text-sm">
                            ${item.name} · <fmt:formatNumber value="${item.amount}" pattern="#,##0"/>
                        </span>
                    </c:forEach>
                    <c:if test="${empty report.tagExpenseSummary}">
                        <span class="text-sm text-slate-500">태그별 집계 데이터가 없습니다.</span>
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
