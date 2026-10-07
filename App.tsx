import React, {useCallback, useEffect, useState} from 'react';
import {AppState, Button, NativeModules, PermissionsAndroid, SafeAreaView, Text, View} from 'react-native';

const {QuickPanel} = NativeModules;

export default function App() {
  const [status, setStatus] = useState('');
  const [overlayOk, setOverlayOk] = useState(false);
  const [locationOk, setLocationOk] = useState(false);
  const [bgOk, setBgOk] = useState(false);

  const refresh = useCallback(async () => {
    setOverlayOk(await QuickPanel.canDrawOverlays());
    setLocationOk(await PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION));
    setBgOk(await PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION));
  }, []);

  useEffect(() => {
    refresh();
    const sub = AppState.addEventListener('change', s => s === 'active' && refresh());
    return () => sub.remove();
  }, [refresh]);

  const add = async (type: 'wifi' | 'volume') => {
    try {
      const r = await QuickPanel.requestAddTile(type);
      setStatus(r === 'unsupported'
        ? 'Android 12: swipe down twice, tap the pencil icon, and drag the tile in.'
        : `Result: ${r}`);
    } catch (e: any) { setStatus(`Error: ${e.message}`); }
  };

  const askLocation = async () => {
    await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION);
    refresh();
  };

  const askBackground = async () => {
    await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION);
    refresh();
  };

  return (
    <SafeAreaView style={{flex: 1, padding: 24, justifyContent: 'center'}}>
      <Text style={{fontSize: 22, fontWeight: '600', marginBottom: 8}}>Quick Panel</Text>
      <Text style={{marginBottom: 16}}>
        Step 1: grant permissions. Step 2: add the tiles to your notification shade.
      </Text>
      <View style={{gap: 12}}>
        <Button title={`Location (shows Wi-Fi name) ${locationOk ? '✓' : ''}`} onPress={askLocation} />
        <Button title={`Location: allow all the time (tile Wi-Fi name) ${bgOk ? '✓' : ''}`} onPress={askBackground} />
        <Button title={`Display over other apps (volume popup) ${overlayOk ? '✓' : ''}`} onPress={() => QuickPanel.openOverlaySettings()} />
        <Button title="Add Wi-Fi tile" onPress={() => add('wifi')} />
        <Button title="Add Volume tile" onPress={() => add('volume')} />
      </View>
      {!!status && <Text style={{marginTop: 20}}>{status}</Text>}
    </SafeAreaView>
  );
}
