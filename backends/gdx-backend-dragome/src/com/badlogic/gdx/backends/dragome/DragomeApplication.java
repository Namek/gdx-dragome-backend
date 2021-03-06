/*******************************************************************************
 * Copyright 2016 Natan Guilherme.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.dragome;

import com.badlogic.gdx.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import com.badlogic.gdx.backends.dragome.preloader.AssetDownloader;
import com.badlogic.gdx.backends.dragome.preloader.AssetDownloader.AssetLoaderListener;
import com.badlogic.gdx.backends.dragome.preloader.AssetType;
import com.badlogic.gdx.backends.dragome.preloader.Preloader;
import com.badlogic.gdx.backends.dragome.soundmanager2.SoundManager;
import com.badlogic.gdx.backends.dragome.utils.AgentInfo;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.ObjectMap;
import com.dragome.commons.javascript.ScriptHelper;
import com.dragome.view.DefaultVisualActivity;
import com.dragome.web.enhancers.jsdelegate.JsCast;
import com.dragome.web.html.dom.w3c.BrowserDomHandler;
import com.dragome.web.html.dom.w3c.HTMLCanvasElementExtension;

/** @author xpenatan */
public abstract class DragomeApplication extends DefaultVisualActivity implements Application {

	private ApplicationListener listener;
	BrowserDomHandler elementBySelector;

	DragomeApplicationConfiguration config;
	DragomeGraphics graphics;
	DragomeInput input;
	DragomeNet net;
	DragomeFiles files;
	DragomeAudio audio;
	Clipboard clipboard;
	int init = 0;
	Preloader preloader;
	private int logLevel = LOG_INFO;
	private Array<Runnable> runnables = new Array<>();
	private Array<Runnable> runnablesHelper = new Array<>();
	private Array<LifecycleListener> lifecycleListeners = new Array<>();
	private ObjectMap<String, Preferences> prefs = new ObjectMap<>();
	private int lastWidth;
	private int lastHeight;
	private static AgentInfo agentInfo;
	ApplicationLogger applicationLogger;

	@Override
	public void build () {
		elementBySelector = new BrowserDomHandler();
		prepare();
	}

	private void prepare () {
		preloader = new Preloader(AssetDownloader.getHostPageBaseURL() + "assets/");
		AssetLoaderListener<Object> assetListener = new AssetLoaderListener<>();
		AssetDownloader.loadScript("soundmanager2-jsmin.js", assetListener);
		getPreloader().loadAsset("com/badlogic/gdx/graphics/g3d/shaders/default.fragment.glsl", AssetType.Text, null, assetListener);
		getPreloader().loadAsset("com/badlogic/gdx/graphics/g3d/shaders/default.vertex.glsl", AssetType.Text, null, assetListener);
		getPreloader().loadAsset("com/badlogic/gdx/graphics/g3d/shaders/depth.fragment.glsl", AssetType.Text, null, assetListener);
		getPreloader().loadAsset("com/badlogic/gdx/graphics/g3d/shaders/depth.vertex.glsl", AssetType.Text, null, assetListener);
		getPreloader().loadAsset("com/badlogic/gdx/utils/arial-15.fnt", AssetType.Text, null, assetListener);
		getPreloader().loadAsset("com/badlogic/gdx/utils/arial-15.png", AssetType.Image, null, assetListener);
		listener = createApplicationListener();
		if (listener == null) return;
		config = getConfig();
		if(config == null)
			config = new DragomeApplicationConfiguration();
		graphics = new DragomeGraphics(this, config);
		if (!graphics.init()) {
			error("DragomeApplication", "WebGL not supported.");
			return;
		}

		input = new DragomeInput(this);
		net = new DragomeNet();
		files = new DragomeFiles(preloader);
		audio = new DragomeAudio();
		Gdx.app = this;
		Gdx.graphics = graphics;
		Gdx.gl20 = graphics.getGL20();
		Gdx.gl = Gdx.gl20;
		Gdx.input = input;
		Gdx.net = net;
		Gdx.files = files;
		Gdx.audio = audio;
		DragomeApplication.agentInfo = AgentInfo.computeAgentInfo();

		if(agentInfo.isWindows() == true)
			System.setProperty("os.name", "Windows");
		else if(agentInfo.isMacOS() == true)
			System.setProperty("os.name", "OS X");
		else if(agentInfo.isLinux() == true)
			System.setProperty("os.name", "Linux");
		else
			System.setProperty("os.name", "no OS");

		this.clipboard = new DragomeClipboard();
		lastWidth = graphics.getWidth();
		lastHeight = graphics.getHeight();

		onResize();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Document document = elementBySelector.getDocument();
		EventTarget doc = JsCast.castTo(document, EventTarget.class);
		EventListener eventListener = new EventListener() {
			@Override
			public void handleEvent (Event evt) {
				onVisibilityChange(!ScriptHelper.evalBoolean("document.hidden", this));
			}
		};

		doc.addEventListener("visibilitychange", eventListener, false);

//		new Timer().setInterval(new Runnable() {
//			public void run () {
//				try {
//					mainLoop();
//				} catch (Throwable t) {
//					error("DragomeApplication", "exception: " + t.getMessage(), t);
//					throw new RuntimeException(t);
//				}
//			}
//		}, 0);

		DragomeWindow.requestAnimationFrame(new Runnable() {

			@Override
			public void run () {
				try {
					if(init != 2) { // #TODO not the best solution but a working solution.
						if(AssetDownloader.getQueue() == 0) {
							if(init == 0) {
								init = 1;
								// initialize SoundManager2
								SoundManager.init(getAssetUrl(), 9, config.preferFlash, new SoundManager.SoundManagerCallback() {
									@Override
									public void onready () {
										graphics.update();
										onResize();
										lastWidth = graphics.getWidth();
										lastHeight = graphics.getHeight();
										Gdx.gl.glViewport(0, 0, lastWidth, lastHeight);
										listener.create();
										DragomeApplication.this.listener.resize(graphics.getWidth(), graphics.getHeight());
										DragomeWindow.onResize(new Runnable() {
											@Override
											public void run () {
												onResize();
												DragomeApplication.this.listener.resize(graphics.getWidth(), graphics.getHeight());
											}
										});
										init = 2;
									}

									@Override
									public void ontimeout (String status, String errorType) {
										error("SoundManager", status + " " + errorType);
									}
								});
							}
						}
					}
					else
						mainLoop();
				} catch (Throwable t) {
					ScriptHelper.put("t", t, this);
					ScriptHelper.put("message", t.getMessage(), this);
					ScriptHelper.evalNoResult("var stack = t.$$$stackTrace___java_lang_String;", this);
					ScriptHelper.evalNoResult("var error = new Error(message);", this);
					ScriptHelper.evalNoResult("error.stack = stack;", this);
					ScriptHelper.evalNoResult("throw error;", this);
				}
				DragomeWindow.requestAnimationFrame(this, graphics.canvas);
			}
		}, graphics.canvas);

		applicationLogger =  new ApplicationLogger() {
			@Override
			public void log (String tag, String message) {
				System.out.println(tag + ": " + message);
			}

			@Override
			public void log (String tag, String message, Throwable exception) {
				System.out.println(tag + ": " + message);
				exception.printStackTrace(System.out);
			}

			@Override
			public void error (String tag, String message) {
				ScriptHelper.put("tag", tag, this);
				ScriptHelper.put("message", message, this);
				ScriptHelper.evalNoResult("console.error(tag + ': ' + message)", this);
			}

			@Override
			public void error (String tag, String message, Throwable exception) {
				ScriptHelper.put("tag", tag, this);
				ScriptHelper.put("message", message, this);
				ScriptHelper.evalNoResult("console.error(tag + ': ' + message)", this);
				exception.printStackTrace(System.err);
			}

			@Override
			public void debug (String tag, String message) {
				ScriptHelper.put("tag", tag, this);
				ScriptHelper.put("message", message, this);
				ScriptHelper.evalNoResult("console.warn(tag + ': ' + message)", this);
			}

			@Override
			public void debug (String tag, String message, Throwable exception) {
				ScriptHelper.put("tag", tag, this);
				ScriptHelper.put("message", message, this);
				ScriptHelper.evalNoResult("console.warn(tag + ': ' + message)", this);
				exception.printStackTrace(System.out);
			}
		};
	}

	protected void onResize () {}

	private void mainLoop () {
		graphics.update();
		if (Gdx.graphics.getWidth() != lastWidth || Gdx.graphics.getHeight() != lastHeight) {
			DragomeApplication.this.listener.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			lastWidth = graphics.getWidth();
			lastHeight = graphics.getHeight();
			Gdx.gl.glViewport(0, 0, lastWidth, lastHeight);
		}
		runnablesHelper.addAll(runnables);
		runnables.clear();
		for (int i = 0; i < runnablesHelper.size; i++) {
			runnablesHelper.get(i).run();
		}
		runnablesHelper.clear();
		graphics.frameId++;
		listener.render();
		input.reset();
	}

	public abstract ApplicationListener createApplicationListener ();

	public abstract DragomeApplicationConfiguration getConfig ();

	@Override
	public ApplicationListener getApplicationListener () {
		return listener;
	}

	@Override
	public Graphics getGraphics () {
		return graphics;
	}

	@Override
	public Audio getAudio () {
		return audio;
	}

	@Override
	public Input getInput () {
		return input;
	}

	@Override
	public Files getFiles () {
		return files;
	}

	@Override
	public Net getNet () {
		return net;
	}

	@Override
	public void log (String tag, String message) {
		if (logLevel >= LOG_INFO)
			applicationLogger.log(tag, message);
	}

	@Override
	public void log (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_INFO)
			applicationLogger.log(tag, message, exception);
	}

	@Override
	public void error (String tag, String message) {
		if (logLevel >= LOG_ERROR)
			applicationLogger.error(tag, message);
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR)
			applicationLogger.error(tag, message, exception);
	}

	@Override
	public void debug (String tag, String message) {
		if (logLevel >= LOG_DEBUG)
			applicationLogger.debug(tag, message);
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG)
			applicationLogger.debug(tag, message, exception);
	}

	@Override
	public void setLogLevel (int logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public int getLogLevel () {
		return logLevel;
	}

	@Override
	public ApplicationType getType () {
		return ApplicationType.WebGL;
	}

	@Override
	public int getVersion () {
		return 0;
	}

	@Override
	public long getJavaHeap () {
		return 0;
	}

	@Override
	public long getNativeHeap () {
		return 0;
	}

	@Override
	public void setApplicationLogger (ApplicationLogger applicationLogger) {
		this.applicationLogger = applicationLogger;
	}

	@Override
	public ApplicationLogger getApplicationLogger () {
		return applicationLogger;
	}

	@Override
	public Preferences getPreferences (String name) {
		Preferences pref = prefs.get(name);
		if (pref == null) {
			pref = new DragomePreferences(name);
			prefs.put(name, pref);
		}
		return pref;
	}

	@Override
	public Clipboard getClipboard () {
		return clipboard;
	}

	@Override
	public void postRunnable (Runnable runnable) {
		runnables.add(runnable);
	}

	@Override
	public void exit () {
	}

	@Override
	public void addLifecycleListener (LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	@Override
	public void removeLifecycleListener (LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.removeValue(listener, true);
		}
	}

	private void onVisibilityChange (boolean visible) {
		if (visible) {
			for (LifecycleListener listener : lifecycleListeners) {
				listener.resume();
			}
			listener.resume();
		} else {
			for (LifecycleListener listener : lifecycleListeners) {
				listener.pause();
			}
			listener.pause();
		}
	}

	public HTMLCanvasElementExtension getCanvas () {
		return graphics.canvas;
	}

	public String getAssetUrl () {
		return preloader.baseUrl;
	}

	public Preloader getPreloader () {
		return preloader;
	}

	public void addEventListener (EventListener aEventListener, String aEvent) {
		Element theElement = elementBySelector.getElementBySelector("body");
		EventTarget eventTarget= JsCast.castTo(theElement, EventTarget.class);
		eventTarget.addEventListener(aEvent, aEventListener, false);
	}

	/** Contains precomputed information on the user-agent. Useful for dealing with browser and OS behavioral differences. Kindly
	 * borrowed from PlayN */
	public static AgentInfo agentInfo () {
		return agentInfo;
	}
}
