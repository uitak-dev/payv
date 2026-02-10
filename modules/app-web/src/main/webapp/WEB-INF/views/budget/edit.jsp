<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>예산 수정</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="plan" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <a href="${ctx}/budget/budgets/${budget.budgetId}" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">뒤로</a>
            <h1 class="text-base font-semibold">예산 수정</h1>
            <span class="w-12"></span>
        </div>
    </header>

    <main class="px-4 pt-4 pb-24">
        <c:if test="${not empty error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <form method="post"
              action="${ctx}/budget/budgets/${budget.budgetId}"
              class="space-y-4"
              data-ajax="true"
              data-method="PUT"
              data-json="true">
            <section class="pv-card p-4">
                <div class="grid gap-4">
                    <label>
                        <div class="text-xs text-slate-500">대상 월 (필수)</div>
                        <input name="month" type="month" required value="${budget.targetMonth}" class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                    </label>

                    <label>
                        <div class="text-xs text-slate-500">목표 금액 (필수)</div>
                        <input name="amountLimit" type="number" min="1" inputmode="numeric" required value="${budget.amountLimit}" class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                    </label>

                    <label>
                        <div class="text-xs text-slate-500">카테고리 (선택, 미선택 시 전체 예산)</div>
                        <select name="categoryId" class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                            <option value="">전체 예산</option>
                            <c:forEach var="root" items="${categories}">
                                <option value="${root.categoryId}" ${root.categoryId == budget.categoryId ? 'selected' : ''}>${root.name}</option>
                            </c:forEach>
                        </select>
                    </label>

                    <label>
                        <div class="text-xs text-slate-500">메모 (선택)</div>
                        <textarea name="memo" rows="2" class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">${budget.memo}</textarea>
                    </label>
                </div>
            </section>

            <button type="submit" class="w-full rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white">수정 저장</button>
        </form>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
