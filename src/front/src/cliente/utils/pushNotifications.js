const API_URL = import.meta.env.VITE_API_URL

function urlBase64ToUint8Array(base64String) {

    const padding = '='.repeat((4 - base64String.length % 4) % 4)

    const base64 = (base64String + padding)
        .replace(/-/g, '+')
        .replace(/_/g, '/')

    const rawData = window.atob(base64)

    return Uint8Array.from([...rawData].map(char => char.charCodeAt(0)))
}

export async function registrarPushNotifications(tokenPublico) {

    if (!('serviceWorker' in navigator)) {
        console.warn('Service Worker no soportado')
        return
    }

    if (!('PushManager' in window)) {
        console.warn('Push API no soportada')
        return
    }

    try {

        console.log('Iniciando push notifications')

        const permiso = await Notification.requestPermission()

        console.log('Permiso:', permiso)

        if (permiso !== 'granted') {
            console.warn('Permiso de notificaciones denegado')
            return
        }

        // Registrar service worker
        console.log('Registrando SW...')

        const registration = await navigator.serviceWorker.register('/sw.js')

        console.log('SW registrado:', registration)

        // Obtener VAPID public key
        const response = await fetch(
            `${API_URL}/api/public/push/public-key`
        )

        const data = await response.json()

        const vapidPublicKey = data.publicKey

        console.log('VAPID:', vapidPublicKey)

        // Crear subscription
        const subscription = await registration.pushManager.subscribe({
            userVisibleOnly: true,
            applicationServerKey: urlBase64ToUint8Array(vapidPublicKey)
        })

        const subscriptionJson = subscription.toJSON()

        console.log('Subscription:', subscriptionJson)

        // Enviar subscription al backend
        await fetch(
            `${API_URL}/api/public/turnos/${tokenPublico}/push-subscriptions`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    endpoint: subscriptionJson.endpoint,
                    p256dh: subscriptionJson.keys.p256dh,
                    auth: subscriptionJson.keys.auth
                })
            }
        )

        console.log('Push notifications registradas correctamente')

    } catch (error) {

        console.error('Error registrando push notifications:', error)

    }
}