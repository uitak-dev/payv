<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>로그인</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="login" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <div class="text-base font-semibold">로그인</div>
            <c:if test="${not empty authenticatedUserId}">
                <form method="post" action="${ctx}/logout" data-ajax="true">
                    <button type="submit" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">로그아웃</button>
                </form>
            </c:if>
        </div>
    </header>

    <main class="px-4 pb-8 pt-4">
        <div class="space-y-6 pt-10">
            <div>
                <h2 class="text-xl font-semibold">Payv 로그인</h2>
                <p class="mt-1 text-sm text-slate-500">세션 기반 인증</p>
            </div>

            <c:if test="${not empty error}">
                <p class="rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">로그인에 실패했습니다.</p>
            </c:if>
            <c:if test="${not empty logout}">
                <p class="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">로그아웃 되었습니다.</p>
            </c:if>
            <c:if test="${not empty success}">
                <p class="rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">로그인되었습니다.</p>
            </c:if>
            <c:if test="${not empty signupSuccess}">
                <p class="rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">회원가입이 완료되었습니다. 로그인해 주세요.</p>
            </c:if>

            <form class="space-y-4" method="post" action="${ctx}/perform_login" data-ajax="true">
                <label class="block">
                    <div class="text-sm font-medium">이메일</div>
                    <input name="email" type="email" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2" placeholder="example@example.com"/>
                </label>
                <label class="block">
                    <div class="text-sm font-medium">비밀번호</div>
                    <input name="password" type="password" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2" placeholder="••••••••"/>
                </label>
                <button type="submit" class="mt-2 inline-flex w-full items-center justify-center rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white">로그인</button>
            </form>
            <a href="${ctx}/signup" class="inline-flex w-full items-center justify-center rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700">회원가입</a>

            <c:if test="${not empty authenticatedUserId}">
                <div class="rounded-xl border border-slate-200 p-3 text-sm">
                    <div class="font-medium">인증된 사용자: ${authenticatedUserId}</div>
                    <div class="mt-2 flex flex-wrap gap-2">
                        <a class="rounded-lg border border-slate-200 px-3 py-1.5" href="${ctx}/ledger/transactions">거래</a>
                        <a class="rounded-lg border border-slate-200 px-3 py-1.5" href="${ctx}/asset/assets">자산</a>
                        <a class="rounded-lg border border-slate-200 px-3 py-1.5" href="${ctx}/classification/categories">카테고리</a>
                        <a class="rounded-lg border border-slate-200 px-3 py-1.5" href="${ctx}/classification/tags">태그</a>
                    </div>
                </div>
            </c:if>
        </div>
    </main>
</div>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
