<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>홈 / 원장</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="home" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <h1 class="text-base font-semibold">홈</h1>
        </div>
    </header>

    <main class="px-4 pt-4 pv-safe-bottom">
        <div class="space-y-4">
            <section class="pv-card-subtle p-4">
                <div class="flex items-start justify-between gap-3">
                    <div>
                        <div class="text-xs text-slate-500">이번 달 남은 예산</div>
                        <div class="mt-1 flex items-end gap-2">
                            <div class="text-2xl font-semibold">
                                <fmt:formatNumber value="${remainingBudget}" pattern="#,##0"/>
                            </div>
                            <span class="pv-badge"><span class="pv-dot"></span>사용률 ${budgetUsageRate}%</span>
                        </div>
                        <div class="mt-2 pv-bar" style="--w: ${budgetUsageRate > 100 ? 100 : budgetUsageRate}%"><span></span></div>
                        <c:if test="${not hasOverallBudget}">
                            <div class="mt-2 text-xs text-slate-500">이번 달 전체 예산이 아직 설정되지 않았습니다.</div>
                        </c:if>
                    </div>
                    <a href="${ctx}/budget/budgets?month=${month}" class="rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white">예산 보기</a>
                </div>

                <div class="mt-4 grid grid-cols-3 gap-2">
                    <div class="pv-card p-3">
                        <div class="text-xs text-slate-500">총수입</div>
                        <div class="mt-1 text-sm font-semibold pv-amount positive">+<fmt:formatNumber value="${incomeAmount}" pattern="#,##0"/></div>
                    </div>
                    <div class="pv-card p-3">
                        <div class="text-xs text-slate-500">총지출</div>
                        <div class="mt-1 text-sm font-semibold pv-amount negative">-<fmt:formatNumber value="${expenseAmount}" pattern="#,##0"/></div>
                    </div>
                    <div class="pv-card p-3">
                        <div class="text-xs text-slate-500">순현금흐름</div>
                        <div class="mt-1 text-sm font-semibold"><fmt:formatNumber value="${netAmount}" pattern="#,##0"/></div>
                    </div>
                </div>
            </section>

            <section class="pv-card">
                <div class="border-b border-slate-200 px-4 py-3 text-sm font-semibold">최근 거래 (${monthStart} ~ ${monthEnd})</div>
                <div class="divide-y divide-slate-100">
                    <c:forEach var="item" items="${recentTransactions}">
                        <a href="${ctx}/ledger/transactions/${item.transactionId}" class="flex items-center justify-between px-4 py-3">
                            <div class="flex items-center gap-3">
                                <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-100">
                                    <img src="${ctx}/assets/icons/receipt.svg" alt="" class="h-5 w-5 opacity-80" aria-hidden="true"/>
                                </div>
                                <div>
                                    <div class="text-sm font-medium">${item.categoryNameLevel1}</div>
                                    <div class="mt-0.5 text-xs text-slate-500">${item.transactionDate} · ${item.assetName}</div>
                                    <div class="mt-0.5 text-xs text-slate-500">${item.memo}</div>
                                </div>
                            </div>
                            <div class="text-sm font-semibold ${item.transactionType == 'EXPENSE' ? 'pv-amount negative' : 'pv-amount positive'}">
                                <c:choose>
                                    <c:when test="${item.transactionType == 'EXPENSE'}">-</c:when>
                                    <c:otherwise>+</c:otherwise>
                                </c:choose>
                                <fmt:formatNumber value="${item.amount}" pattern="#,##0"/>
                            </div>
                        </a>
                    </c:forEach>
                    <c:if test="${empty recentTransactions}">
                        <div class="px-4 py-3 text-sm text-slate-500">이번 달 거래가 없습니다.</div>
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
