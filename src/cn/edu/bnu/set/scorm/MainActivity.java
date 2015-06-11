package cn.edu.bnu.set.scorm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.edu.bnu.set.scorm.R;
import cn.edu.bnu.set.scorm.Fragment.AboutFragment;
import cn.edu.bnu.set.scorm.Fragment.ContactUsFragment;
import cn.edu.bnu.set.scorm.Fragment.CourseFragment;
import cn.edu.bnu.set.scorm.Fragment.CourseGroupFragment;
import cn.edu.bnu.set.scorm.Fragment.MainFragment;
import cn.edu.bnu.set.scorm.Fragment.UserFragment;
import cn.edu.bnu.set.scorm.Net.CourseListWorker;
import cn.edu.bnu.set.scorm.Util.PrePreference;
import cn.edu.bnu.set.scorm.Util.ZoomDrawable;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
	public static final String ARG_OPTIONS_NUMBER = "option_number";

	public static final int MODULE_USER_TO_COURSE = 2;
	public static final int MODULE_COURSE_TO_COURSE_UNIT = 3;
	public static final int MODULE_COURSE_UNIT_GROUP_TO_COURSE_UNIT = 4;
	public static final int MODULE_COURSE_GROUP_TO_COURSE = 5;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mTitles;
	private String[] mCoursesTitles = { "000" };

	private JSONArray courses;
	private JSONArray courseGroups;
	private JSONObject openCourseGropuJSON;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FragmentManager fm = getFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.content_frame);

		fragment = new MainFragment();
		fm.beginTransaction().add(R.id.content_frame, fragment).commit();
		mTitle = mDrawerTitle = getTitle();
		mTitles = getResources().getStringArray(R.array.options_array);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ItemsAdapter(this, 0, mTitles));
		mDrawerList.setOnItemClickListener(new DrawerMainItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		// ����MainActivity��������ʼ��ȡ�γ��б����棡
		// ��SharedPreference�л�ȡ�û���id���ڻ�ȡ�γ��б�
		// ��������ļ�ĩβ��˽�з���
		initCourseLists();
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { MenuInflater
	 * inflater = getMenuInflater(); inflater.inflate(R.menu.main, menu); return
	 * super.onCreateOptionsMenu(menu); }
	 */

	/* Called whenever we call invalidateOptionsMenu() */
	/*
	 * @Override public boolean onPrepareOptionsMenu(Menu menu) { // If the nav
	 * drawer is open, hide action items related to the content // view boolean
	 * drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
	 * menu.findItem(R.id.action_websearch).setVisible(!drawerOpen); return
	 * super.onPrepareOptionsMenu(menu); }
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_websearch:
			// create intent to perform web search for this planet
			Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
			intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
			// catch event that there's no activity to handle intent
			if (intent.resolveActivity(getPackageManager()) != null) {
				startActivity(intent);
			} else {
				Toast.makeText(this, R.string.app_not_available,
						Toast.LENGTH_LONG).show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerMainItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectMainItem(position);
		}
	}

	private class DrawerCoursesItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectCourseItem(position);
		}
	}

	private void selectMainItem(int position) {
		// update the main content by replacing fragments
		Bundle args = new Bundle();
		Fragment fragment = null;
		switch (position) {
		case 0:
			fragment = new UserFragment();
			break;
		case 1:
			if (mCoursesTitles.length <= 1) {
				Toast.makeText(this, "��ȡ�γ��б�������!\n�������ӻ���ϵ����Ա",
						Toast.LENGTH_SHORT).show();
				return;
			}
			mDrawerList.setAdapter(new ItemsAdapter(this, 0, mCoursesTitles));
			mDrawerList
					.setOnItemClickListener(new DrawerCoursesItemClickListener());
			return;
		case 2:
			fragment = new ContactUsFragment();
			break;
		case 3:
			fragment = new AboutFragment();
			break;
		default:
			break;
		}

		fragment.setArguments(args);
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment).commit();

		// update selected item and title, then close the drawer
		setTitle(mTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);

	}

	private void selectCourseItem(int position) {
		// ���ݿγ̵����ԣ��γ̵�Ԫ��γ��飩���жϽ��еĲ���
		// �γ����������һ���˵�
		// �γ�������µ�Fragment
		int listLength = courses.length() + courseGroups.length();

		if (position < courses.length()) {
			Bundle args = new Bundle();
			Fragment fragment = null;
			try {
				int courseId = courses.getJSONObject(position).getInt("id");
				openCourseGropuJSON = new PrePreference(this)
						.getCourseUnitList(courseId);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			// �򿪿γ���ʾ�γ̵�Ԫ�б�
			fragment = new CourseFragment();
			args.putString(CourseFragment.ARG_COURSE_JSON,
					openCourseGropuJSON.toString());
			fragment.setArguments(args);
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();
			// update selected item and title, then close the drawer
			setTitle(mCoursesTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else if (position >= courses.length() && position < listLength) {
			// ѡ����ǿγ���
			// ���ڽ�����һ���˵���Ϊ���ӣ���һ�ַ�ʽʵ��
			// ����һ��CourseGroupFragment��������ʾ�ÿγ����µĿγ̺Ϳγ���
			// ������Ƶ��ý�Ϊ����
			Bundle args = new Bundle();
			Fragment fragment = null;
			try {
				int courseGroupId = courseGroups.getJSONObject(
						position - courses.length()).getInt("id");
				openCourseGropuJSON = new PrePreference(this)
						.getGroupCourseList(courseGroupId);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			fragment = new CourseGroupFragment();
			args.putString(CourseGroupFragment.ARG_COURSE_JSON,
					openCourseGropuJSON.toString());
			fragment.setArguments(args);
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();
			setTitle(mCoursesTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else if (position == courses.length() + courseGroups.length()) {
			// ѡ����Ƿ��ذ�ť
			// �˻���һ���˵�
			mDrawerList.setAdapter(new ItemsAdapter(this, 0, mTitles));
			mDrawerList
					.setOnItemClickListener(new DrawerMainItemClickListener());
			setTitle(R.string.app_name);
			return;
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private class ItemsAdapter extends ArrayAdapter<String> {

		public ItemsAdapter(Context context, int layoutResourceId, String[] list) {
			super(context, layoutResourceId, list);
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = MainActivity.this.getLayoutInflater().inflate(
						R.layout.drawer_list_item, null);
			}
			String title = getItem(pos);
			Drawable icon = getResources().getDrawable(getItemIconId(title));
			icon = ZoomDrawable.zoomDrawable(icon, 280, 280);
			((TextView) convertView.findViewById(R.id.item_name))
					.setText(title);
			((ImageView) convertView.findViewById(R.id.item_icon))
					.setImageDrawable(icon);
			return convertView;
		}
	}

	private int getItemIconId(String item) {
		int id = 0;
		switch (item) {
		case "�ҵ���Ϣ":
			id = R.drawable.user;
			break;
		case "�γ��б�":
			id = R.drawable.course;
			break;
		case "ѧϰ��Դ":
			id = R.drawable.source;
			break;
		case "����":
			id = R.drawable.about;
			break;
		case "��ϵ����":
			id = R.drawable.contact;
			break;
		case "�������˵�":
			id = R.drawable.back;
			break;
		default:
			id = R.drawable.course;
			break;
		}
		return id;
	}

	/**
	 * ��CourseListWorker�ص��ķ���
	 * 
	 * @throws JSONException
	 */
	@SuppressLint("UseValueOf")
	public void setCourseLists(JSONObject list) throws JSONException {
		courses = list.getJSONArray("course");

		courseGroups = list.getJSONArray("courseGroup");

		int tmpLength = new Integer(courses.length())
				+ new Integer(courseGroups.length()) + 1;

		String[] tmp = new String[tmpLength];
		int i = 0;
		for (; i < courses.length(); i++) {
			// ��ʼ���γ��б���Ӧ���б���γ̱���������ʾ
			// ͬʱʹ�ÿγ�idȥ��ȡ�γ��µĿγ̵�Ԫ�б����棬�ȴ�����
			tmp[i] = "���γ̡�" + courses.getJSONObject(i).getString("title");
			new CourseListWorker(this, MODULE_COURSE_TO_COURSE_UNIT, courses
					.getJSONObject(i).getInt("id")).execute();
		}
		for (int j = 0; j < courseGroups.length(); i++, j++) {
			tmp[i] = "���γ��顿" + courseGroups.getJSONObject(j).getString("title");
			new CourseListWorker(this, MODULE_COURSE_GROUP_TO_COURSE,
					courseGroups.getJSONObject(j).getInt("id")).execute();
		}
		tmp[i] = getResources().getString(R.string.course_back_main);
		mCoursesTitles = tmp;
		new PrePreference(this).saveUserCourseList(list);
	}

	private void initCourseLists() {
		new CourseListWorker(this, MODULE_USER_TO_COURSE, new PrePreference(
				this).getUser().getId()).execute();
	}

	public void setCourseUnitLists(JSONObject list, int courseId)
			throws JSONException {
		new PrePreference(this).saveCourseUnitList(list, courseId);
	}

	public void setCourseListOfCourseGroup(JSONObject list, int groupId)
			throws JSONException {
		new PrePreference(this).saveGroupCourseList(list, groupId);
	}
}