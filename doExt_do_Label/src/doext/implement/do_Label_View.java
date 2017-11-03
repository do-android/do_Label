package doext.implement;

import java.util.Map;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import core.helper.DoTextHelper;
import core.helper.DoUIModuleHelper;
import org.json.JSONObject;
import core.interfaces.DoIScriptEngine;
import core.interfaces.DoIUIModuleView;
import core.object.DoInvokeResult;
import core.object.DoUIModule;
import doext.define.do_Label_IMethod;
import doext.define.do_Label_MAbstract;

/**
 * 自定义扩展UIView组件实现类，此类必须继承相应VIEW类，并实现DoIUIModuleView,Do_Label_IMethod接口；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.model.getUniqueKey());
 */
public class do_Label_View extends TextView implements DoIUIModuleView, do_Label_IMethod {

	/**
	 * 每个UIview都会引用一个具体的model实例；
	 */
	private do_Label_MAbstract model;

	public do_Label_View(Context context) {
		super(context);
	}

	/**
	 * 初始化加载view准备,_doUIModule是对应当前UIView的model实例
	 */
	@Override
	public void loadView(DoUIModule _doUIModule) throws Exception {
		this.model = (do_Label_MAbstract) _doUIModule;
		this.setTextSize(TypedValue.COMPLEX_UNIT_PX, DoUIModuleHelper.getDeviceFontSize(_doUIModule, "17"));
		this.setTextColor(Color.BLACK);
		this.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		this.setMaxLines(1);
		this.setEllipsize(TruncateAt.END);
	}

	/**
	 * 动态修改属性值时会被调用，方法返回值为true表示赋值有效，并执行onPropertiesChanged，否则不进行赋值；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public boolean onPropertiesChanging(Map<String, String> _changedValues) {
		return true;
	}

	/**
	 * 属性赋值成功后被调用，可以根据组件定义相关属性值修改UIView可视化操作；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@SuppressLint("NewApi")
	@Override
	public void onPropertiesChanged(Map<String, String> _changedValues) {
		DoUIModuleHelper.handleBasicViewProperChanged(this.model, _changedValues);
		DoUIModuleHelper.setFontProperty(this.model, _changedValues);

		if (_changedValues.containsKey("textFlag")) {
			try {
				DoUIModuleHelper.setFontStyle(this, this.model.getPropertyValue("fontStyle"));
				invalidate();
			} catch (Exception e) {

			}
		}

		if (_changedValues.containsKey("textAlign")) {
			String _textAlign = _changedValues.get("textAlign");
			if (_textAlign.equals("center")) {
				this.setGravity(Gravity.CENTER);
			} else if (_textAlign.equals("right")) {
				this.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
			} else {
				this.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			}
		}

		if (_changedValues.containsKey("maxWidth")) {
			int _maxWidth = (int) (DoTextHelper.strToDouble(_changedValues.get("maxWidth"), 100) * this.model.getXZoom());
			this.setMaxWidth(_maxWidth);
		}

		if (_changedValues.containsKey("maxHeight")) {
			int _maxHeight = (int) (DoTextHelper.strToDouble(_changedValues.get("maxHeight"), 100) * this.model.getYZoom());
			this.setMaxHeight(_maxHeight);
		}

		if (_changedValues.containsKey("maxLines")) {
			int _maxLines = DoTextHelper.strToInt(_changedValues.get("maxLines"), 1);
			if (_maxLines <= 0) {
				this.setMaxLines(Integer.MAX_VALUE);
			} else {
				this.setMaxLines(_maxLines);
			}
		}

		// 设置行间距
		if (_changedValues.containsKey("linesSpace")) {
			float _linesSpace = DoTextHelper.strToInt(_changedValues.get("linesSpace"), 0);
			if (_linesSpace >= 0) {
				this.setLineSpacing(_linesSpace, 1f);// 第一个参数是精确值，后一个是倍数。
			}
		}

		if (_changedValues.containsKey("text") || _changedValues.containsKey("height") || _changedValues.containsKey("visible")) {
			try {
				boolean _isVisible = DoTextHelper.strToBool(model.getPropertyValue("visible"), true);
				if (!_isVisible || ("-1".equals(model.getPropertyValue("height")) && TextUtils.isEmpty(model.getPropertyValue("text")))) {
					this.setVisibility(View.GONE);
				} else {
					this.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (_changedValues.containsKey("shadow")) {
			String[] _data = _changedValues.get("shadow").split(",");
			if (_data.length == 4) {
				float _x = (float) (DoTextHelper.strToFloat(_data[1], 0) * this.model.getXZoom());
				float _y = (float) (DoTextHelper.strToFloat(_data[2], 0) * this.model.getYZoom());
				double radiusZoom = (model.getXZoom() + model.getYZoom()) / 2;
				float _radius = (float) (DoTextHelper.strToInt(_data[3], 0) * radiusZoom);
				if (Build.VERSION.SDK_INT >= 19) {
					this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
				}
				this.setShadowLayer(_radius, _x, _y, DoUIModuleHelper.getColorFromString(_data[0], Color.BLACK));
			}
		}
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		// ...do something
		return false;
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.model.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) {
		// ...do something
		return false;
	}

	/**
	 * 释放资源处理，前端JS脚本调用closePage或执行removeui时会被调用；
	 */
	@Override
	public void onDispose() {
		// ...do something
	}

	/**
	 * 重绘组件，构造组件时由系统框架自动调用；
	 * 或者由前端JS脚本调用组件onRedraw方法时被调用（注：通常是需要动态改变组件（X、Y、Width、Height）属性时手动调用）
	 */
	@Override
	public void onRedraw() {
		this.setLayoutParams(DoUIModuleHelper.getLayoutParams(this.model));
	}

	/**
	 * 获取当前model实例
	 */
	@Override
	public DoUIModule getModel() {
		return model;
	}
}
