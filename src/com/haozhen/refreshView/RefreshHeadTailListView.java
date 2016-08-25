package com.haozhen.refreshView;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.haozhen.refreshlistview_library.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RefreshHeadTailListView extends ListView {

	//��Ϊͷ��Ҫ�Ŷ�� ���Բ��ܼ򵥵Ķ���View����
	private LinearLayout           head;
	private View                   tail;
	private LinearLayout           llhideHead;
	private int                    llhideHead_measuredHeight;
	private int                    tail_measuredHeight;
	private float                  downY = -1;
	private View                   lunboView_to_find_location;
	private int                    lunboView_Onscreen_locationY;
	private final int              PULL_DOWN = 1;// ����״̬
	private final int              RELEASE_STATE = 3; // �ɿ�״̬
	private final int              REFRESHING = 5;// ����ˢ��
	private int                    currentState=PULL_DOWN;//Ĭ��������ˢ��״̬  ��������
	private TextView               tv_head_textState;
	private TextView               tv_text_refresh_time;
	private ImageView              iv_red_arrow;
	private ProgressBar            pb_jindutiao;
	private RotateAnimation        ra_up;
	private RotateAnimation        ra_down;
	private OnRefreshDataListener  listener;
	private boolean                isEnablePullRefresh=false;
	private boolean                isLoadingMore=false;//�Ƿ���ظ�������
	//private boolean                isAddMoreData=false;
	//���캯��1
	public RefreshHeadTailListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initView();
		//��ʼ������   ִ��һ��  ���� ������
		initAnimation();
		
		// add scroll event for load more
		initEvent();
		
	}
	private void initEvent() {
		//Ϊlistview��ӻ����¼�
		setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if(!isLoadingMore){
					return;
				}
				//��һ�� &&��Ϊ�� ֻ ���ظ��� һ��   //
				//System.out.println("��ǰλ��"+getLastVisiblePosition()+"  �ܹ�"+getAdapter().getCount());
				if(getLastVisiblePosition()==getAdapter().getCount()-1&&!isLoadingMore){//��Ϊvisibleposition�Ǵ�0��ʼ��
					//������������һ������   ����ʾ���ظ�������
					//��ʾ ���ظ������ݵ���ͼ
					tail.setPadding(0,0, 0, 0);
					setSelection(getAdapter().getCount());
					//System.out.println("���ظ������ݡ���������");
					//������ȥ���ظ��������
					if(listener!=null){
						//�ı��²���������״̬
						isLoadingMore=true;
						listener.loadingMore();
					}					
				}
			 
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	//���캯��2
	public RefreshHeadTailListView(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}
	//���캯��3
	public RefreshHeadTailListView(Context context) {
		this(context,null);
	}
	private void initView() {
		// TODO Auto-generated method stub
		initHead();
		initTail();
	}
   //������� ����Ƶ�ϵ�����  û��Ч�� --��  
	public  void addLunboView(View view){
		head.addView(view);
		
	}
	//���û������Ƿ���������ˢ��  ������true ����false
	public void isUsePullRefresh(boolean ispullrefresh){
		isEnablePullRefresh=ispullrefresh;
	}
	
	//���û�ѡ���Ƿ� ���� ���ظ���Ĺ��� 
	public void isLoadMoreData(boolean isloadingmoredata){
		isLoadingMore = isloadingmoredata;
	}
	
	//��дaddHeaderView����
	@Override
	public void addHeaderView(View view) {
		//���ʹ������ˢ�� 
		if(isEnablePullRefresh){
			lunboView_to_find_location = view;			
			head.addView(view);
		}else{
			//����ʹ��  ��ʹ��listview��ԭ�����ط���
			super.addHeaderView(view);
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.AbsListView#onTouchEvent(android.view.MotionEvent)
	 * �����ص�ˢ��ͷ��ˢ��β ������
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		  switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			if(!isEnablePullRefresh){
				//if we don't use pull refresh
				//just  break
				break;
			}
			if(currentState==REFRESHING){
				break;
			}
			// ��ȡlistview���ֲ�ͼ����Ļ�е����꣬���ж��ֲ�ͼ�Ƿ���ȫ��ʾ
			if(!isLunboFullShow()){
				//���û����ȫ��ʾ
				//ֱ��break��������listvIEW����Ӧ�¼�
				break;
			}

            if(downY==-1){//��ֹ���µ�ʱ��û�л�ȡ���������
            	downY = ev.getY();
            }
			
			float moveY = ev.getY();
			// �������϶�ʱ�������Լ����¼�������listview��ԭ���¼���Ч
			
			float dy = moveY - downY;
			//dy��ʾ�϶��ľ���
			if (dy > 0 && getFirstVisiblePosition() == 0) {
				// �϶��˶��� �Ϸ���¶��������
				float scrollYDis = -llhideHead_measuredHeight + dy;
				if (scrollYDis < 0&&currentState!=PULL_DOWN) {
					// ��ʱ ˢ��ͷ��û����ȫ����ʾ����
				   currentState=PULL_DOWN;
                   refreshState();
				} else if(scrollYDis>=0&&currentState!=RELEASE_STATE){
					currentState=RELEASE_STATE;
				   refreshState();
				}
				llhideHead.setPadding(0, (int) scrollYDis, 0, 0);
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:   ///�ɿ�ʱ�Ĳ���
			downY=-1;
			if(currentState==PULL_DOWN){
			    llhideHead.setPadding(0, -llhideHead_measuredHeight, 0, 0);
			}else if(currentState==RELEASE_STATE){
				//��ʱˢ������
				llhideHead.setPadding(0, 0, 0, 0);
				currentState=REFRESHING;
				refreshState();
				
			}
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}
	//��ͷ����ת����
	private void initAnimation(){
		ra_up = new RotateAnimation(0, -180, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		ra_up.setDuration(500);
		ra_up.setFillAfter(true);
		ra_down = new RotateAnimation(-180, -360, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		ra_up.setDuration(500);
		ra_up.setFillAfter(true);
	}
	private void refreshState() {
		switch (currentState) {
		case PULL_DOWN:
			
			tv_head_textState.setText("����ˢ��");
			//����ˢ��Ӧѡ�� ���ϵĶ���
			iv_red_arrow.startAnimation(ra_down);
			break;
		case RELEASE_STATE:
			
			tv_head_textState.setText("�ɿ�ˢ��");
			iv_red_arrow.startAnimation(ra_up);
			break;
		case REFRESHING:
			iv_red_arrow.clearAnimation();
			iv_red_arrow.setVisibility(View.GONE);//���ؼ�ͷ
			pb_jindutiao.setVisibility(View.VISIBLE);//��ʾ������
			tv_head_textState.setText("����ˢ������");
			//��ʱ��ˢ�������������ˣ�(������)
			if(listener!=null){
				listener.refreshData();
			}
			
		default:
			break;
		}
		
	}
	//����һ���ӿڣ��������������Ǹ���ʵ������ӿ�     �¶�~~
	public interface OnRefreshDataListener{
		//�÷������������������Ǹ�����ʵ�ֵ�~
		void refreshData();
		//�ٶ���һ���ӿ��еķ��� ����listview�ĵײ� ���ظ��������
		void loadingMore();
	}
	public void setOnRefreshDataListener(OnRefreshDataListener listener){
		this.listener=listener;
	}
	//ˢ��״̬���
	public void refreshStateFinish(){
		
		if (isLoadingMore) {
			isLoadingMore=false;
			tail.setPadding(0, -tail_measuredHeight, 0, 0);
			
			

		} else {
			tv_head_textState.setText("����ˢ��");
			// ����ˢ��Ӧѡ�� ���ϵĶ���
			// iv_red_arrow.startAnimation(ra_down);
			iv_red_arrow.setVisibility(View.VISIBLE);// ��ʼ����ͷ
			pb_jindutiao.setVisibility(View.INVISIBLE);// ���ؽ�����
			// ����ˢ��ʱ��Ϊ��ǰ ��ʱ��
			tv_text_refresh_time.setText(getCurrentFormatDate());
			// ����ͷ����
			llhideHead.setPadding(0, -llhideHead_measuredHeight, 0, 0);
			// state value must be initialed too
			currentState = PULL_DOWN;
		}
	}
	private String getCurrentFormatDate(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd HH-mm-ss");
		return format.format(new Date());
	}
	private Boolean isLunboFullShow() {
		//���û���� �ֲ�ͼ
		if(lunboView_to_find_location==null){
			return true;
		}
		//��ȡlistview���ֲ�ͼ����Ļ�е����꣬���ж��ֲ�ͼ�Ƿ���ȫ��ʾ  
		int location[] = new int[2];
		// listview����Ļ�е�����
		if(lunboView_Onscreen_locationY==0){							
		   this.getLocationOnScreen(location);
		   //��ȡһ��listView��Y��߶�
		   lunboView_Onscreen_locationY=location[1];
		}
		
		// �ֲ�ͼ����Ļ�е�����

		lunboView_to_find_location.getLocationOnScreen(location);

		if(location[1]<lunboView_Onscreen_locationY){
			//˵�� �ֲ�ͼ��û����ȫ��ʾ
			//�������ӦlistView��ʱ��			
			return false;
		}
		return true;
	}
	//��ʼ��ͷ��
	private void initHead(){
		head = (LinearLayout) View.inflate(getContext(), R.layout.head_refresh_listview, null);
		
		llhideHead = (LinearLayout) head.findViewById(R.id.ll_head_listView_root);
		tv_head_textState = (TextView) head.findViewById(R.id.desc_listview_refresh);
		tv_text_refresh_time = (TextView) head.findViewById(R.id.desc_listview_refresh_time);
		iv_red_arrow = (ImageView) head.findViewById(R.id.red_arrow);
		pb_jindutiao = (ProgressBar) head.findViewById(R.id.jindutiao);
		//����ˢ��ͷ�ĸ�����
		//���ˢ��ͷ����ĸ߶�
		llhideHead.measure(0, 0);//���߷��� �����Լ�ȥ����
		llhideHead_measuredHeight = llhideHead.getMeasuredHeight();
		//����ˢ��ͷ������  ����ӦΪ ��
		head.setPadding(0, -llhideHead_measuredHeight, 0, 0);
		addHeaderView(head);
	}
	//��ʼ��β��
    private void initTail(){
		tail = View.inflate(getContext(), R.layout.tail_refresh_listview, null);
		tail.measure(0, 0);
		tail_measuredHeight = tail.getMeasuredHeight();
		tail.setPadding(0, -tail_measuredHeight, 0, 0);
		
		addFooterView(tail);
	}

}
