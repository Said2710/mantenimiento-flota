<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Cuenta bloqueada</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-50 min-h-screen flex items-center justify-center px-4">
    <div class="w-full max-w-[480px] bg-white rounded-xl shadow-xl p-8 border border-slate-100 text-center">
        <h1 class="text-2xl font-bold text-red-700">Cuenta bloqueada</h1>
        <p class="text-sm text-slate-600 mt-3">
            Se superaron los 3 intentos fallidos. Por seguridad, el acceso fue bloqueado
            y esta ventana se cerrará en <span id="seg" class="font-bold">5</span> segundos.
        </p>
        <p class="text-xs text-slate-500 mt-4">Contacte al administrador para desbloquear su cuenta.</p>
    </div>

    <script>
        var segundos = 5;
        var contador = document.getElementById('seg');
        var reloj = setInterval(function () {
            segundos--;
            contador.textContent = segundos;
            if (segundos <= 0) {
                clearInterval(reloj);
                window.close();
                // Si el navegador no permite cerrar la pestaña, se deja en blanco
                setTimeout(function () { location.replace('about:blank'); }, 300);
            }
        }, 1000);
    </script>
</body>
</html>