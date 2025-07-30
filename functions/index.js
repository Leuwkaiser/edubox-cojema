/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

// Función simple para enviar notificación push real
exports.enviarNotificacionPushReal = functions.https.onCall(async (data, context) => {
    try {
        const { token, titulo, mensaje, emailDestinatario } = data;
        
        if (!token || !titulo || !mensaje) {
            throw new functions.https.HttpsError('invalid-argument', 'Faltan parámetros: token, titulo, mensaje');
        }

        // Configuración de la notificación push
        const message = {
            token: token,
            notification: {
                title: titulo,
                body: mensaje
            },
            android: {
                notification: {
                    sound: 'default',
                    channelId: 'default_channel',
                    priority: 'high',
                    defaultSound: true,
                    defaultVibrateTimings: true
                }
            },
            data: {
                email: emailDestinatario || '',
                tipo: 'push_real',
                timestamp: Date.now().toString()
            }
        };

        // Enviar la notificación
        const response = await admin.messaging().send(message);
        console.log('✅ Notificación push enviada exitosamente a:', emailDestinatario, 'MessageId:', response);
        
        return { 
            success: true, 
            messageId: response,
            destinatario: emailDestinatario
        };
    } catch (error) {
        console.error('❌ Error al enviar notificación push:', error);
        throw new functions.https.HttpsError('internal', `Error: ${error.message}`);
    }
});

// Función para enviar a múltiples usuarios
exports.enviarNotificacionPushAGrupo = functions.https.onCall(async (data, context) => {
    try {
        const { tokens, titulo, mensaje } = data;
        
        if (!tokens || !Array.isArray(tokens) || tokens.length === 0) {
            throw new functions.https.HttpsError('invalid-argument', 'Tokens requeridos');
        }

        const message = {
            notification: {
                title: titulo,
                body: mensaje
            },
            android: {
                notification: {
                    sound: 'default',
                    channelId: 'default_channel',
                    priority: 'high'
                }
            },
            tokens: tokens
        };

        const response = await admin.messaging().sendMulticast(message);
        console.log('✅ Notificaciones enviadas:', response.successCount, 'de', tokens.length);
        
        return { 
            success: true, 
            successCount: response.successCount,
            failureCount: response.failureCount
        };
    } catch (error) {
        console.error('❌ Error al enviar notificaciones:', error);
        throw new functions.https.HttpsError('internal', 'Error al enviar notificaciones');
    }
});

// Trigger automático cuando se crea notificación en Firestore
exports.onNotificationCreated = functions.firestore
    .document('notificaciones/{notificacionId}')
    .onCreate(async (snap, context) => {
        try {
            const notificacion = snap.data();
            const { titulo, mensaje, destinatarioEmail, tipo } = notificacion;
            
            // Obtener token FCM del destinatario
            const db = admin.firestore();
            const tokenDoc = await db.collection('fcm_tokens').doc(destinatarioEmail).get();
            
            if (tokenDoc.exists) {
                const tokenData = tokenDoc.data();
                const token = tokenData.token;
                const activo = tokenData.activo !== false;
                
                if (token && activo) {
                    const message = {
                        token: token,
                        notification: {
                            title: titulo,
                            body: mensaje
                        },
                        android: {
                            notification: {
                                sound: 'default',
                                channelId: 'default_channel',
                                priority: 'high'
                            }
                        },
                        data: {
                            email: destinatarioEmail,
                            tipo: tipo || 'firestore',
                            notificacionId: context.params.notificacionId
                        }
                    };

                    const response = await admin.messaging().send(message);
                    console.log('✅ Notificación automática enviada a:', destinatarioEmail, 'MessageId:', response);
                } else {
                    console.log('⚠️ Token inactivo para:', destinatarioEmail);
                }
            } else {
                console.log('⚠️ No se encontró token FCM para:', destinatarioEmail);
            }
        } catch (error) {
            console.error('❌ Error en notificación automática:', error);
        }
    });

// Función para limpiar tokens FCM inactivos
exports.limpiarTokensInactivos = functions.pubsub.schedule('every 24 hours').onRun(async (context) => {
    try {
        const db = admin.firestore();
        const tokensRef = db.collection('fcm_tokens');
        
        // Buscar tokens marcados como inactivos
        const snapshot = await tokensRef.where('activo', '==', false).get();
        
        if (!snapshot.empty) {
            const batch = db.batch();
            snapshot.docs.forEach(doc => {
                batch.delete(doc.reference);
            });
            
            await batch.commit();
            console.log(`Se eliminaron ${snapshot.size} tokens inactivos`);
        }
        
        return null;
    } catch (error) {
        console.error('Error al limpiar tokens inactivos:', error);
        return null;
    }
});

// Función para obtener estadísticas de tokens FCM
exports.obtenerEstadisticasTokens = functions.https.onCall(async (data, context) => {
    try {
        const db = admin.firestore();
        const tokensRef = db.collection('fcm_tokens');
        
        const [tokensActivos, tokensInactivos] = await Promise.all([
            tokensRef.where('activo', '==', true).get(),
            tokensRef.where('activo', '==', false).get()
        ]);
        
        return {
            totalTokens: tokensActivos.size + tokensInactivos.size,
            tokensActivos: tokensActivos.size,
            tokensInactivos: tokensInactivos.size
        };
    } catch (error) {
        console.error('Error al obtener estadísticas:', error);
        throw new functions.https.HttpsError('internal', 'Error al obtener estadísticas');
    }
});
