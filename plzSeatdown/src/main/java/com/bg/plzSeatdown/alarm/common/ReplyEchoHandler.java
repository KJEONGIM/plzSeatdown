package com.bg.plzSeatdown.alarm.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.bg.plzSeatdown.member.model.vo.Member;

public class ReplyEchoHandler extends TextWebSocketHandler{
	List<WebSocketSession> sessions = new ArrayList<>();
	
	// 로그인 되어 있는 회원 저장용 Map
	Map<String, WebSocketSession> userSessionsMap = new HashMap<String, WebSocketSession>();
	
	// 서버에 접속 성공했을때
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception{ // 소켓에 연결될 때
		//System.out.println("afterConnectionEstablished : "+ session);
		sessions.add(session);
		
		String senderId = getId(session);
		userSessionsMap.put(senderId, session);
	}
	
	// 멤버 아이디 가져오기
	private String getId(WebSocketSession session) {
		Map<String, Object> httpSession = session.getAttributes();
		Member loginMember = (Member)httpSession.get("loginMember");
		//System.out.println(loginMember);
		if(loginMember == null) {
			return session.getId();
		}else {
			return loginMember.getMemberId();
		}
	}
	
	// 소켓에 메세지를 보낼때
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{ 
		
		try {
		// protocol : cmd, 댓글 작성자, 글 작성자, 글 번호, 글 제목
		String msg = message.getPayload();
		if(!StringUtils.isEmpty(msg)) {
			String[] strs = message.getPayload().split(",");
			String ct = null;
			if(strs != null && strs.length == 5) {
				String cmd = strs[0];
				String replyWriterNick = strs[1];
				String communityWriterId = strs[2];
				String communityTitle = strs[3];
				String cno = strs[4];
				
				WebSocketSession communityWriterSession = userSessionsMap.get(communityWriterId);
				
				if(communityTitle.length()>=7) {
					ct = communityTitle.substring(0, 7);				
				}else {
					ct = communityTitle;
				}
				if("reply".equals(cmd) && communityWriterSession != null) {
					TextMessage tmpMsg = new TextMessage(replyWriterNick + "님이 " + "<a href='/plzSeatdown/community/detail?no=" + cno + "&currentPage=1'> ["+ct+"...]</a> 게시글에 댓글을 남겼습니다.");
					communityWriterSession.sendMessage(tmpMsg);
				}
			}
			
			if(strs != null && strs.length == 4) {
				String li = strs[0];
				String thwriter = strs[1];
				String thNm = strs[2];
				String thCode = strs[3];
				
				WebSocketSession reviewSession = userSessionsMap.get(thwriter);
				
				if("like".equals(li) && reviewSession != null) {
					TextMessage tmpMsg = new TextMessage("<a href='/plzSeatdown/review/seats?thCode=" + thCode + "' > ["+thNm+"...]</a> 리뷰에 좋아요가 눌렸습니다.");
					reviewSession.sendMessage(tmpMsg);
				}
			}
			
		}
		}catch(Exception e) {
			// 예외무시 처리
		}
		
	}
	
	// 연결 해제
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
		userSessionsMap.remove(session.getId());
		sessions.remove(session);
	}
}
