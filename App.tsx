import React, {useState} from 'react';
import {Button, NativeModules, SafeAreaView, Text, View} from 'react-native';

const {QuickPanel} = NativeModules;

export default function App() {
  const [status, setStatus] = useState('');

  const add = async (type: 'wifi' | 'volume') => {
    try {
      const r = await QuickPanel.requestAddTile(type);
      setStatus(
        r === 'unsupported'
          ? 'Android 12: swipe down twice, tap the pencil icon, and drag the tile in.'
          : `Result: ${r}`,
      );
    } catch (e: any) {
      setStatus(`Error: ${e.message}`);
    }
  };

  return (
    <SafeAreaView style={{flex: 1, padding: 24, justifyContent: 'center'}}>
      <Text style={{fontSize: 22, fontWeight: '600', marginBottom: 8}}>Quick Panel</Text>
      <Text style={{marginBottom: 24}}>
        Add tiles to your notification shade. The Wi-Fi tile opens the system network
        popup; the Volume tile lets you switch between Music and Call volume.
      </Text>
      <View style={{gap: 12}}>
        <Button title="Add Wi-Fi tile" onPress={() => add('wifi')} />
        <Button title="Add Volume tile" onPress={() => add('volume')} />
      </View>
      {!!status && <Text style={{marginTop: 20}}>{status}</Text>}
    </SafeAreaView>
  );
}
